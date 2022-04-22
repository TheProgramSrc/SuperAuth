package xyz.theprogramsrc.superauth_v3.spigot.handlers;

import static xyz.theprogramsrc.superauth_v3.spigot.objects.AuthMethod.COMMANDS;
import static xyz.theprogramsrc.superauth_v3.spigot.objects.AuthMethod.GUI;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth_v3.global.SessionStorage;
import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.superauth_v3.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth_v3.spigot.objects.AuthMethod;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class AuthHandler extends SpigotModule {

    private final Player player;
    private User user;

    public AuthHandler(Player player){
        this.player = player;
        SuperAuth.spigot.getUserStorage().get(player.getName(), user -> {
            this.user = user;
            this.handle();
        });
    }

    private void handle(){
        this.getSpigotTasks().runAsyncTask(() -> {
            this.debug("Initializing Auth Handler for user '" + this.player.getName() + "'");
            if(this.shouldSkipAuth()){
                new ActionManager(this.player).after(true);
            }else{
                this.execute();
            }
        });
    }

    public boolean shouldSkipAuth(){
        if(this.user.isRegistered()){
            if((this.user.isPremium() && SuperAuth.spigot.getAuthSettings().getPremiumAutoLogin()) || (this.hasValidSession() && SuperAuth.spigot.getAuthSettings().isSessionsEnabled())){
                return true;
            }
        }

        return false;
    }

    private void execute(){
        this.getSpigotTasks().runAsyncTask(this::beforeAuth);
        if(this.getAuthMethod() == COMMANDS){ // Command Auth Handler
            new CommandAuthHandler(this.player, this.user);
        }else if(this.getAuthMethod() == GUI){ // GUI Auth Handler
            new GUIAuthHandler(this.player, this.user);
        }else{ // Dialog Auth Handler
            new DialogAuthHandler(this.player, this.user);
        }
    }

    private AuthMethod getAuthMethod(){
        if(!this.user.isRegistered()) return SuperAuth.spigot.getAuthSettings().getAuthMethod();
        return AuthMethod.of(this.user.getAuthMethod());
    }

    private void beforeAuth(){
        if(this.getAuthMethod() == COMMANDS){
            this.getSuperUtils().sendMessage(
                    player,
                    LBase.COMMAND_HOW_TO_USE
                            .options()
                            .placeholder("{RegisterCommand}", SuperAuth.spigot.getAuthSettings().getRegisterCommand().toLowerCase())
                            .placeholder("{LoginCommand}", SuperAuth.spigot.getAuthSettings().getLoginCommand().toLowerCase())
                            .toString()
            );
        }else if(this.getAuthMethod() == GUI){
            this.getSuperUtils().sendMessage(
                    player,
                    LBase.GUI_HOW_TO_USE
                            .options()
                            .placeholder("{Command}", SuperAuth.spigot.getAuthSettings().getAuthCommand().toLowerCase())
                            .toString()
            );
        }else{
            this.getSuperUtils().sendMessage(player, LBase.DIALOG_HOW_TO_USE.toString());
        }
    }

    private boolean hasValidSession(){
        String path = this.user.getIp() + this.player.getUniqueId();
        if(SessionStorage.i.has(path)){
            String data = SessionStorage.i.get(path);
            long maxTime = SuperAuth.spigot.getAuthSettings().getSessionMaxTime() * 1000L;
            long lastTime;
            try {
                lastTime = Long.parseLong(data);
            }catch (NumberFormatException e){
                lastTime = 0L;
            }

            if((System.currentTimeMillis() - lastTime) <= maxTime){
                return true;
            }
            SessionStorage.i.remove(path);
        }

        return false;
    }
}
