package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

public class SkinSyncListener extends SpigotModule {

    @Override
    public void onLoad() {
        final UserStorage userStorage = SuperAuth.spigot.getUserStorage();
        if(!this.getPlugin().getPluginDataStorage().isLowResourceUsageEnabled()){
            this.getSpigotTasks().runRepeatingTask(0L, Utils.toTicks(60), () -> {
                for(final Player player : Bukkit.getOnlinePlayers()){
                    this.getSpigotTasks().runAsyncTask(() -> {
                        if(Utils.isConnected()){
                            User user = userStorage.get(player.getName());
                            if(user != null){
                                if(!user.hasSkin()){
                                    String skin;
                                    try{
                                        skin = this.spigotPlugin.getSkinManager().getSkin(player).toString();
                                    }catch (Exception ignored){
                                        skin = "no_skin";
                                    }


                                    user.setSkinTexture(skin);
                                    userStorage.save(user);
                                }
                            }
                        }
                    });
                }
            });
        }
    }
}
