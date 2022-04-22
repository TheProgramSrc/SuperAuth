package xyz.theprogramsrc.superauth_v3.bungee.commands;

import java.util.ArrayList;
import java.util.UUID;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.theprogramsrc.superauth_v3.bungee.SuperAuth;
import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.supercoreapi.bungee.commands.BungeeCommand;
import xyz.theprogramsrc.supercoreapi.bungee.commands.CommandResult;
import xyz.theprogramsrc.supercoreapi.bungee.utils.BungeeConsole;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;

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
        this.getBungeeTasks().runAsync(() -> {
            this.userStorage.get(player.getName(), user -> {
                if(user == null){
                    this.getSuperUtils().sendMessage(player, LBase.ERROR_FETCHING_DATA.toString());
                }else{
                    if(!user.isPremium()){
                        this.getSuperUtils().sendMessage(player, LBase.ALREADY_CRACKED.toString());
                    }else{
                        if(!player.hasPermission(SuperAuth.bungee.getCrackedPermission())){
                            this.getSuperUtils().sendMessage(player, "&c" + Base.NO_PERMISSION);
                        }else{
                            if(!this.waiting.contains(player.getUniqueId())){
                                this.getSuperUtils().sendMessage(player, LBase.CONFIRMATION_MESSAGE.options().placeholder("{Command}", "/" + SuperAuth.bungee.getCrackedCommand().toLowerCase()).toString());
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
            });
        });
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(BungeeConsole console, String[] args) {
        return CommandResult.NOT_SUPPORTED;
    }
}
