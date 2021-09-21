package xyz.theprogramsrc.superauth.spigot.listeners;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;

import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class IPSyncListener extends SpigotModule {

    private static final LinkedList<UUID> cache = new LinkedList<>();
    private static final SuperAuth superAuth = SuperAuth.spigot;
    private static final UserStorage userStorage = superAuth.getUserStorage();

    @Override
    public void onLoad() {
        if(!Utils.isConnected() && this.getPlugin().getPluginDataStorage().isLowResourceUsageEnabled()) return;
        this.getSpigotTasks().runAsyncRepeatingTask(0L, Utils.toTicks(120), () -> Bukkit.getOnlinePlayers().stream().filter(player -> player.getAddress() != null && !cache.contains(player.getUniqueId())).forEach(player -> {
            String ip = player.getAddress().getAddress().getHostAddress();
            userStorage.get(player.getName(), user -> {
                if(user == null) return;
                if(!user.getIp().equals("null")){ // Check if the ip is null
                    userStorage.save(user.setIp(ip), () -> {
                        if(!superAuth.getVPNBlocker().isVPN(ip)) return;
                        cache.add(player.getUniqueId());
                        this.getSpigotTasks().runTask(() -> player.kickPlayer(this.getSuperUtils().color(LBase.VPN_KICK.toString())));
                    });
                }else if(!ip.equals(user.getIp()) && superAuth.getAuthSettings().isBlockIPChanges()){
                    this.getSpigotTasks().runTask(() -> player.kickPlayer(this.getSuperUtils().color(LBase.YOUR_IP_HAS_CHANGED.options().placeholder("{NewIPAddress}", ip).get())));
                }
            });
        }));
    }
}
