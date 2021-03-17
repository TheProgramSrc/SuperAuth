package xyz.theprogramsrc.superauth.bungee.commands;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import xyz.theprogramsrc.superauth.bungee.SuperAuth;
import xyz.theprogramsrc.supercoreapi.bungee.commands.BungeeCommand;
import xyz.theprogramsrc.supercoreapi.bungee.commands.CommandResult;
import xyz.theprogramsrc.supercoreapi.bungee.utils.BungeeConsole;

public class MainCommand extends BungeeCommand {

    @Override
    public String getCommand() {
        return "superauth";
    }

    @Override
    public CommandResult onPlayerExecute(ProxiedPlayer player, String[] args) {
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(BungeeConsole console, String[] args) {
        if(args.length == 0){
            this.getSuperUtils().sendMessage(console.parseCommandSender(), "&cPlease check the wiki: &bhttps://wiki.theprogrmsrc.xyz/");
            return CommandResult.COMPLETED;
        }else{
            if(args[0].toLowerCase().equals("reload")){
                SuperAuth.bungee.loadSettings();
                this.getSuperUtils().sendMessage(console.parseCommandSender(), "&aSuccessfully reloaded settings.");
                return CommandResult.COMPLETED;
            }else{
                return CommandResult.INVALID_ARGS;
            }
        }
    }
}
