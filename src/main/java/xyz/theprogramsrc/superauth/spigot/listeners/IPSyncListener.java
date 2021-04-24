package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class IPSyncListener extends SpigotModule {

    @Override
    public void onLoad() {
        if(Utils.isConnected()){
            final SuperAuth superAuth = SuperAuth.spigot;
            final UserStorage userStorage = superAuth.getUserStorage();
            this.getSpigotTasks().runRepeatingTask(0L, Utils.toTicks(60), () -> {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(Utils.isConnected() && player != null && player.getAddress() != null){
                        new Thread(() -> {
                            User user = userStorage.get(player.getName());
                            if(user != null){
                                String ip = user.getIp() == null ? "null" : user.getIp();
                                String playerIp = player.getAddress().getAddress().getHostAddress();
                                if(!ip.equalsIgnoreCase("null")){
                                    user.setIp(playerIp);
                                    userStorage.save(user);
                                    if(superAuth.getVPNBlocker().isVPN(playerIp)){
                                        this.getSpigotTasks().runTask(() -> player.kickPlayer(this.getSuperUtils().color(LBase.VPN_KICK.toString())));
                                    }
                                }else if(!playerIp.equals(ip)){
                                    this.getSpigotTasks().runTask(() -> player.kickPlayer(this.getSuperUtils().color(LBase.YOUR_IP_HAS_CHANGED.options().placeholder("{NewIPAddress}", ip).get())));
                                }
                            }
                        }).start();
                    }
                }
            });
        }
    }
}
