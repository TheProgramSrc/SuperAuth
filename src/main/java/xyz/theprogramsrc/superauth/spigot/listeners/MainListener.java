package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.theprogramsrc.superauth.global.SessionStorage;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.handlers.AuthHandler;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.superauth.spigot.storage.DatabaseMigration;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

import java.util.Objects;

public class MainListener extends SpigotModule {

    private UserStorage userStorage;
    private AuthSettings settings;
    
    @Override
    public void onLoad() {
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.settings = SuperAuth.spigot.getAuthSettings();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event){
        if(!DatabaseMigration.migrating){ // Ignore the event if we're migrating
            this.getSpigotTasks().runAsyncTask(() -> this.handleAuth(event.getPlayer(), true));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(final PlayerQuitEvent event){
        if(!DatabaseMigration.migrating) { // Ignore the event if we're migrating
            Player player = event.getPlayer();
            String ip = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
            this.getSpigotTasks().runAsyncTask(() -> {
                User user = this.userStorage.get(player.getName());
                if(user == null) return;
                if(player.getAddress() != null && user.isAuthorized() && user.isRegistered()){
                    SessionStorage.i.set(ip + player.getUniqueId(), System.currentTimeMillis()+"");
                }
                this.userStorage.removeCache(player.getName());
            });
        }
    }

    public void onReload(){
        for(Player player : Bukkit.getOnlinePlayers()){
            this.getSpigotTasks().runAsyncTask(() -> this.handleAuth(player, false));
        }
    }

    private void handleAuth(Player player, boolean disableAuthorization){
        String ip = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        this.getSpigotTasks().runAsyncTask(() -> {
            User user = this.userStorage.get(player.getName());
            if(user == null) return;
            if(user.isAuthorized() && disableAuthorization){
                user.setAuthorized(false);
                user = this.userStorage.saveAndGet(user);
            }

            if((user.getIp() == null || user.getIp().equals("") || user.getIp().equals(" ") || user.getIp().equals("null"))){
                user.setIp(ip);
                user = this.userStorage.saveAndGet(user);
            }

            if(this.validateIpAddress(player, user, ip)){
                this.checkSkin(user, player);
                if(!this.settings.isAuthEnabled()) return;
                this.executeAntiBot(player, user);

                new AuthHandler(player);
            }
        });
    }

    private void executeAntiBot(final Player player, User user){
        int max = this.settings.getMaxTime();
        this.getSpigotTasks().runTaskLater(Utils.toTicks(max), ()->{
            User currentUser = this.userStorage.get(user.getUsername());
            if(currentUser != null){
                if(!currentUser.isAuthorized()){
                    player.closeInventory();
                    player.kickPlayer(this.getSuperUtils().color(LBase.TOOK_TOO_LONG.options().vars(max+"").placeholder("{Time}", max+"").toString())); // Remove var in v3.17
                }
            }
        });
    }

    private void checkSkin(User user, Player player){
        this.getSpigotTasks().runAsyncTask(() -> {
            if(Utils.isConnected()){
                if(user == null) return;
                if(!user.hasSkin()){
                    SkinTexture skin = this.spigotPlugin.getSkinManager().getSkin(player);
                    if(skin == null)
                        return;
                    user.setSkinTexture(skin.toString());
                    this.userStorage.save(user);
                }
            }
        });
    }

    private boolean validateIpAddress(final Player player, User user, String ip){
        if(user.getIp() != null && !user.getIp().equals("") && !user.getIp().equals(" ") && !user.getIp().equals("null") && this.settings.isBlockIPChanges()){
            if(!ip.equals(user.getIp())){
                this.getSpigotTasks().runTask(() -> player.kickPlayer(this.getSuperUtils().color(LBase.YOUR_IP_HAS_CHANGED.options().placeholder("{NewIPAddress}", ip).get())));
                return false;
            }
        }
        return true;
    }
}
