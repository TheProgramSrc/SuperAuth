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

    private UserStorage userStorage;
    private SuperAuth superAuth;

    @Override
    public void onLoad() {
        if(Utils.isConnected()){
            this.superAuth = SuperAuth.spigot;
            this.userStorage = this.superAuth.getUserStorage();
            Thread thread = new Thread(() -> this.getSpigotTasks().runRepeatingTask(0L, Utils.toTicks(60), this::sync));
            thread.setPriority(3);
            thread.start();
        }
    }

    public void sync(){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player == null)
                continue;
            User user = this.userStorage.get(player.getName());
            if(user == null)
                continue;

            if(player.getAddress() == null)
                continue;

            String ip = user.getIp() == null ? "null" : user.getIp();
            if(ip.equalsIgnoreCase("null")){
                String playerIp = player.getAddress().getAddress().getHostAddress();
                user.setIp(playerIp);
                this.userStorage.save(user);
                if(this.superAuth.getVPNBlocker().isVPN(playerIp)){
                    player.kickPlayer(this.getSuperUtils().color(LBase.VPN_KICK.toString()));
                }
            }
        }
    }
}
