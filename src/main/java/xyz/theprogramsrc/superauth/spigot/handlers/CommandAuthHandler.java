package xyz.theprogramsrc.superauth.spigot.handlers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.CaptchaMemory;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class CommandAuthHandler extends SpigotModule {

    public CommandAuthHandler(Player player, User user){
        this.debug("Loading user '" + player.getName() + "' with " + this.getClass().getSimpleName());

        UserStorage userStorage = SuperAuth.spigot.getUserStorage();
        AuthSettings authSettings = SuperAuth.spigot.getAuthSettings();
        ActionManager actionManager = new ActionManager(player);

        if(!user.isRegistered()){
            actionManager.before(false);
            new BukkitRunnable(){
                @Override
                public void run() {
                    User finalUser = userStorage.get(player.getName());
                    if(finalUser != null){
                        if(!finalUser.isAuthorized()){
                            if(!CaptchaMemory.i.has(player.getName())) {
                                CommandAuthHandler.this.getSuperUtils().sendMessage(player, LBase.REGISTER_COMMAND_USAGE.toString());
                            }else{
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(this.spigotPlugin, 0L, Utils.toTicks(authSettings.getCommandUsageTimer()));
        }else{
            actionManager.before(true);
            user.setAuthorized(false);
            userStorage.save(user);
            new BukkitRunnable(){
                @Override
                public void run() {
                    User finalUser = userStorage.get(player.getName());
                    if(finalUser != null){
                        if(!finalUser.isAuthorized()){
                            if(!CaptchaMemory.i.has(player.getName())) {
                                CommandAuthHandler.this.getSuperUtils().sendMessage(player, LBase.LOGIN_COMMAND_USAGE.toString());
                            }else{
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(this.spigotPlugin, 0L, Utils.toTicks(authSettings.getCommandUsageTimer()));
        }
    }
}
