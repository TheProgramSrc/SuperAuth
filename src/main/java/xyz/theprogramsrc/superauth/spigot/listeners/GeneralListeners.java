package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

public class GeneralListeners extends SpigotModule {

    private SuperAuth superAuth;
    private UserStorage userStorage;
    private boolean connected;
    private long lastCheck;

    @Override
    public void onLoad() {
        this.superAuth = SuperAuth.spigot;
        this.userStorage = this.superAuth.getUserStorage();
        this.connected = Utils.isConnected();
        this.lastCheck = System.currentTimeMillis();

        this.getSpigotTasks().runRepeatingTask(0L, 20L, this::sync);
        this.getSpigotTasks().runRepeatingTask(0L, Utils.toTicks(10), this::ipSYNC);
    }

    public void sync(){
        long millis = System.currentTimeMillis();
        if(millis - this.lastCheck >= 30000L){
            this.connected = Utils.isConnected();
            this.lastCheck = millis;
        }

        if(this.connected){
            for(Player player : Bukkit.getOnlinePlayers()){
                if(player == null)
                    continue;
                User user = this.userStorage.get(player.getName());
                if(user == null)
                    continue;
                if(!user.hasSkin()){
                    SkinTexture skin = this.spigotPlugin.getSkinManager().getSkin(player);
                    if(user.getSkinTexture() == null){
                        user.setSkinTexture(skin != null ? skin.toString() : "no_skin");
                        this.userStorage.save(user);
                    }else if(user.getSkinTexture().equals("no_skin")){
                        user.setSkinTexture(skin != null ? skin.toString() : "no_skin");
                        this.userStorage.save(user);
                    }
                }
            }
        }
    }

    private void ipSYNC(){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player == null)
                continue;
            User user = this.userStorage.get(player.getName());
            if(user == null)
                continue;
            if(user.getIp() == null || user.getIp().equalsIgnoreCase("null")){
                if(player.getAddress() == null)
                    continue;
                String ip = player.getAddress().getAddress().getHostAddress();
                user.setIp(ip);
                this.userStorage.save(user);
                if(this.superAuth.getVPNBlocker().isVPN(ip)){
                    player.kickPlayer(this.getSuperUtils().color(LBase.VPN_KICK.toString()));
                }
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        String username = e.getPlayer().getName();
        this.userStorage.removeCache(username);
    }
}
