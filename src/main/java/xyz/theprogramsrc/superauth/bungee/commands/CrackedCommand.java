package xyz.theprogramsrc.superauth.bungee.commands;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.theprogramsrc.superauth.bungee.SuperAuth;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.supercoreapi.bungee.commands.BungeeCommand;
import xyz.theprogramsrc.supercoreapi.bungee.commands.CommandResult;
import xyz.theprogramsrc.supercoreapi.bungee.utils.BungeeConsole;

import java.util.ArrayList;
import java.util.UUID;

public class CrackedCommand extends BungeeCommand {

    private final UserStorage userStorage;
    private final ArrayList<UUID> waiting;

    public CrackedCommand(){
        this.waiting = new ArrayList<>();
        this.userStorage = SuperAuth.bungee.getUserStorage();
    }

    @Override
    public String getCommand() {
        return SuperAuth.bungee.getCrackedCommand().toLowerCase();
    }

    @Override
    public CommandResult onPlayerExecute(ProxiedPlayer player, String[] args) {
        User user = this.userStorage.get(player.getName());
        if(user == null){
            this.getSuperUtils().sendMessage(player, LBase.ERROR_FETCHING_DATA.toString());
        }else{
            if(!user.isPremium()){
                this.getSuperUtils().sendMessage(player, LBase.ALREADY_CRACKED.toString());
            }else{
                if(!player.hasPermission(SuperAuth.bungee.getCrackedPermission())){
                    return CommandResult.NO_PERMISSION;
                }else{
                    if(!this.waiting.contains(player.getUniqueId())){
                        this.getSuperUtils().sendMessage(player, LBase.CONFIRMATION_MESSAGE.options().vars("/" + SuperAuth.bungee.getCrackedCommand().toLowerCase()).toString());
                        this.getSuperUtils().sendMessage(player, LBase.CHANGE_MODE_WARNING.toString());
                        this.waiting.add(player.getUniqueId());
                    }else{
                        this.waiting.remove(player.getUniqueId());
                        user.setPremium(false);
                        this.userStorage.save(user);
                        player.disconnect(new TextComponent(this.getSuperUtils().color(LBase.CHANGE_MODE_KICK.toString())));
                    }
                }
            }
        }
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(BungeeConsole console, String[] args) {
        return CommandResult.NOT_SUPPORTED;
    }
}
