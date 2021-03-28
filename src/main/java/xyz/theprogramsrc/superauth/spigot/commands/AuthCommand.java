package xyz.theprogramsrc.superauth.spigot.commands;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.guis.auth.IdentifyAuthGUI;
import xyz.theprogramsrc.superauth.spigot.objects.AuthMethod;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.commands.CommandResult;
import xyz.theprogramsrc.supercoreapi.spigot.commands.SpigotCommand;
import xyz.theprogramsrc.supercoreapi.spigot.utils.SpigotConsole;

public class AuthCommand extends SpigotCommand {

    private final UserStorage userStorage;
    private final AuthSettings authSettings;

    public AuthCommand(){
        this.debug("Loading auth command...");
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.debug("Stored UserStorage variable");
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.debug("Stored AuthSettings variable");
    }

    @Override
    public String getCommand() {
        String cmd = SuperAuth.spigot.getAuthSettings().getAuthCommand().toLowerCase();
        this.debug("Registering AuthCommand with command '" + cmd + "'");
        return cmd;
    }

    @Override
    public String[] getAliases() {
        String[] aliases = Utils.toStringArray(SuperAuth.spigot.getAuthSettings().getAuthAliases());
        this.debug("Registering AuthCommand with aliases: " + String.join(", ", aliases));
        return aliases;
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] args) {
        User user = this.userStorage.get(player.getName(), true);
        if(user == null) {
            this.getSuperUtils().sendMessage(player, this.getSettings().getPrefix() + LBase.ERROR_FETCHING_DATA);
        }else{
            if(this.authSettings.getAuthMethod() != AuthMethod.GUI){
                if(user.isRegistered()){
                    if(user.getAuthMethod().equals("GUI")){
                        this.exe(player, user);
                    }
                }
            }else{
                this.exe(player, user);
            }
        }
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(SpigotConsole console, String[] args) {
        return CommandResult.NOT_SUPPORTED;
    }

    private void exe(Player player, User user){
        if(user.isAuthorized()){
            this.getSuperUtils().sendMessage(player, this.getSettings().getPrefix() + LBase.ALREADY_IDENTIFIED);
        }else{
            new IdentifyAuthGUI(player, user);
        }
    }
}
