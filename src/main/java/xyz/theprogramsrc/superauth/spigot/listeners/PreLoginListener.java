package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.global.vpn_blocker.VPNBlocker;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class PreLoginListener extends SpigotModule  {

    private VPNBlocker vpnBlocker;
    private UserStorage userStorage;

    @Override
    public void onLoad() {
        this.vpnBlocker = SuperAuth.spigot.getVPNBlocker();
        this.userStorage = SuperAuth.spigot.getUserStorage();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event){
        String username = event.getName();
        String ip = event.getAddress().getHostAddress();
        if(this.vpnBlocker.isVPN(ip)){
            event.setKickMessage(this.getSuperUtils().color(LBase.VPN_KICK.toString()));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }else{
            User user;
            if(!this.userStorage.exists(username)){
                user = new User(username)
                        .setIp(ip)
                        .setAuthorized(false)
                        .setRegistered(false);
                this.userStorage.save(user);
            }else{
                user = this.userStorage.get(username);
            }


            if(user.isAuthorized()){
                user.setAuthorized(false);
                this.userStorage.save(user);
            }
        }
    }

}
