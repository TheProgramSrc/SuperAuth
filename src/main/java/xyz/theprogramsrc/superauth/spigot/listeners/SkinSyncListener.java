package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

public class SkinSyncListener extends SpigotModule {

    private UserStorage userStorage;

    @Override
    public void onLoad() {
        if(Utils.isConnected()){
            this.userStorage = SuperAuth.spigot.getUserStorage();
            Thread thread = new Thread(() -> this.getSpigotTasks().runRepeatingTask(0L, Utils.toTicks(15), this::sync));
            thread.setPriority(3);
            thread.start();
        }
    }

    private void sync(){
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
