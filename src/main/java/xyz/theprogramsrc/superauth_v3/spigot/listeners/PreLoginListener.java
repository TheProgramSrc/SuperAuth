package xyz.theprogramsrc.superauth_v3.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.global.vpn_blocker.VPNBlocker;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.superauth_v3.spigot.storage.DatabaseMigration;
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
        if(!DatabaseMigration.migrating) { // Ignore the event if we're migrating
            String username = event.getName();
            String ip = event.getAddress().getHostAddress();
            if(this.vpnBlocker.isVPN(ip)){
                event.setKickMessage(this.getSuperUtils().color(LBase.VPN_KICK.toString()));
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            }else{
                
                this.userStorage.exists(username, exists -> {
                    if(!exists){
                        this.userStorage.save(new User(username).setIp(ip).setAuthorized(false).setRegistered(false));
                    }else{
                        this.userStorage.get(username, user -> {
                            if(user.isAuthorized()){
                                user.setAuthorized(false);
                            }
                        });
                    }
                });
            }
        }else{
            event.setKickMessage(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later."));
            event.disallow(Result.KICK_OTHER, this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later."));
        }
    }

}
