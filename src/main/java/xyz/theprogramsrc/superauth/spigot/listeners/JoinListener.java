package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterCaptchaEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeCaptchaEvent;
import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.guis.auth.IdentifyAuthGUI;
import xyz.theprogramsrc.superauth.spigot.handlers.AuthHandler;
import xyz.theprogramsrc.superauth.spigot.handlers.DialogAuthHandler;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.CaptchaMemory;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import static xyz.theprogramsrc.superauth.spigot.objects.AuthMethod.*;

public class JoinListener extends SpigotModule {

    private UserStorage userStorage;
    private AuthSettings settings;
    
    @Override
    public void onLoad() {
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.settings = SuperAuth.spigot.getAuthSettings();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event){
        new Thread(() -> this.handleAuth(event.getPlayer(), true)).start();
    }

    public void onReload(){
        for(Player player : Bukkit.getOnlinePlayers()){
            new Thread(() -> this.handleAuth(player, false)).start();
        }
    }

    private void handleAuth(Player player, boolean disableAuthorization){
        try{
            this.getSpigotTasks().runTaskLater(10, ()->{
                User user = this.userStorage.get(player.getName());
                if(user == null) return;
                if(user.isAuthorized()){
                    if(!disableAuthorization) return;
                    user.setAuthorized(false);
                    this.userStorage.save(user);
                    user = this.userStorage.get(player.getName());
                }
                this.checkSkin(user, player);
                if(!this.settings.isAuthEnabled()) return;
                this.executeAntiBot(player, user);

                new AuthHandler(player);
            });
        }catch (Exception e){
            this.plugin.addError(e);
            e.printStackTrace();
        }
    }

    private void executeAntiBot(final Player player, User user){
        int max = this.settings.getMaxTime();
        this.getSpigotTasks().runTaskLater(Utils.toTicks(max), ()->{
            User currentUser = this.userStorage.get(user.getUsername());
            if(currentUser != null){
                if(!currentUser.isAuthorized()){
                    player.closeInventory();
                    player.kickPlayer(this.getSuperUtils().color(LBase.TOOK_TOO_LONG.options().vars(max+"").toString()));
                }
            }
        });
    }

    private void checkSkin(User user, Player player){
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
    }
}
