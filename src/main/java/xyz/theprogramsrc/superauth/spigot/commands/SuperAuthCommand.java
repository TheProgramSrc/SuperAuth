package xyz.theprogramsrc.superauth.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.guis.account.MyAccountGUI;
import xyz.theprogramsrc.superauth.spigot.guis.admin.AdminGUI;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.supercoreapi.spigot.commands.CommandResult;
import xyz.theprogramsrc.supercoreapi.spigot.commands.precreated.SuperCoreAPICommand;
import xyz.theprogramsrc.supercoreapi.spigot.utils.SpigotConsole;

public class SuperAuthCommand extends SuperCoreAPICommand {

    private final UserStorage userStorage;

    public SuperAuthCommand(){
        this.userStorage = SuperAuth.spigot.getUserStorage();
    }

    @Override
    public String getCommand() {
        return "sauth";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"superauth"};
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] args) {
        User user = this.userStorage.get(player.getName());
        if(args.length == 0){
            if(user.isAdmin()){
                new AdminGUI(player);
            }else{
                if(!player.hasPermission("superauth.my-account")){
                    return CommandResult.NO_PERMISSION;
                }else{
                    new MyAccountGUI(player);
                }
            }
        }else{
            if(args[0].equalsIgnoreCase("info")){
                if(user.isAdmin()){
                    this.executeInfoCommand(player);
                    new Thread(()-> {
                        if(!SuperAuth.spigot.isSQLite()){
                            this.getSuperUtils().sendMessage(player, "&7Testing database connection...");
                            boolean test = SuperAuth.spigot.getDataBase().testConnection();
                            if(test){
                                this.getSuperUtils().sendMessage(player, "&aTest passed!");
                            }else{
                                this.getSuperUtils().sendMessage(player, "&cTest failed. Please check the console for more information.");
                            }
                        }
                    });
                }else{
                    return CommandResult.NO_PERMISSION;
                }
            }else if(args[0].equalsIgnoreCase("paste")){
                if(user.isAdmin()){
                    this.executePasteCommand(player);
                }else{
                    return CommandResult.NO_PERMISSION;
                }
            }else{
                return CommandResult.INVALID_ARGS;
            }
        }
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(SpigotConsole console, String[] args) {
        if(args.length == 0){
            this.log(LBase.WIKI_INFORMATION.toString());
        }else{
            switch (args[0].toLowerCase()) {
                case "updateuser":
                    if(args.length <= 3) {
                        return CommandResult.INVALID_ARGS;
                    }else{
                        String username = args[1];
                        if(!this.userStorage.exists(username)){
                            this.log("&c" + LBase.USER_NOT_EXISTS);
                        }else{
                            User user = this.userStorage.get(username);
                            if (!user.isRegistered()) {
                                this.log("&c" + LBase.USER_NOT_REGISTERED);
                            } else {
                                String val = args[3];
                                switch (args[2].toLowerCase()){
                                    case "ip":
                                        user.setIp(val);
                                        this.userStorage.save(user);
                                        this.log("&a" + LBase.CONSOLE_UPDATED_USER_IP_ADDRESS.options().placeholder("{NewIPAddress}", val).placeholder("{UserName}", user.getUsername()));
                                        return CommandResult.COMPLETED;
                                    case "premium":
                                        user.setPremium(val.equalsIgnoreCase("true"));
                                        boolean mode = user.isPremium();
                                        this.userStorage.save(user);
                                        this.log("&a" + LBase.CONSOLE_UPDATED_USER_MODE.options().placeholder("{NewMode}", (mode ? LBase.PREMIUM : LBase.CRACKED).toString()).placeholder("{UserName}", user.getUsername()));
                                        return CommandResult.COMPLETED;
                                    default:
                                        return CommandResult.INVALID_ARGS;
                                }
                            }
                        }
                    }
                    break;
                case "setadmin":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        if (!this.userStorage.exists(username)) {
                            this.log("&c" + LBase.USER_NOT_EXISTS);
                        } else {
                            User user = this.userStorage.get(username);
                            if (!user.isRegistered()) {
                                this.log("&c" + LBase.USER_NOT_REGISTERED);
                            } else {
                                if (user.isAdmin()) {
                                    this.log(LBase.ALREADY_ADMIN.options().vars(user.getUsername()).get());
                                } else {
                                    user.setAdmin(true);
                                    this.userStorage.save(user);
                                    this.log(LBase.ADDED_ADMIN.options().vars(user.getUsername()).get());
                                }
                            }
                        }
                    }
                    break;
                case "remadmin":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        if (!this.userStorage.exists(username)) {
                            this.log("&c" + LBase.USER_NOT_EXISTS);
                        } else {
                            User user = this.userStorage.get(username);
                            if (!user.isRegistered()) {
                                this.log("&c" + LBase.USER_NOT_REGISTERED);
                            } else {
                                if (!user.isAdmin()) {
                                    this.log(LBase.ALREADY_NON_ADMIN.options().vars(user.getUsername()).get());
                                } else {
                                    user.setAdmin(false);
                                    this.userStorage.save(user);
                                    this.log(LBase.REMOVED_ADMIN.options().vars(user.getUsername()).get());
                                }
                            }
                        }
                    }
                    break;
                case "force-login":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        if (!this.userStorage.exists(username)) {
                            this.log("&c" + LBase.USER_NOT_EXISTS);
                        } else {
                            User user = this.userStorage.get(username);
                            if (!user.isRegistered()) {
                                this.log("&c" + LBase.USER_NOT_REGISTERED);
                            } else {
                                Player player = Bukkit.getPlayer(user.getUsername());
                                if (player == null) {
                                    this.log("&c" + LBase.ERROR_WHILE_FETCHING_PLAYER);
                                } else {
                                    if (user.isAuthorized()) {
                                        this.log("&c" + LBase.USER_ALREADY_IDENTIFIED);
                                    } else {
                                        if(user.getAuthMethod().toLowerCase().contains("dialog")){
                                            this.log("&c" + LBase.FORCE_LOGIN_NOT_SUPPORTED);
                                        }else{
                                            ForceLoginMemory.i.add(player.getName(), "1");
                                            new ActionManager(player).after(true);
                                            this.log("&a" + LBase.FORCED_LOGIN.options().vars(user.getUsername()).get());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "unregister":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        if (!this.userStorage.exists(username)) {
                            this.log("&c" + LBase.USER_NOT_EXISTS);
                        } else {
                            User user = this.userStorage.get(username);
                            if (!user.isRegistered()) {
                                this.log("&c" + LBase.USER_NOT_REGISTERED);
                            } else {
                                this.log("&a" + LBase.REMOVE_REQUEST_SENT);
                                this.userStorage.remove(user);
                                Player p = Bukkit.getPlayer(username);
                                if(p != null){
                                    if(p.isOnline()){
                                        p.kickPlayer("Disconnected");
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "reload":
                    SuperAuth.spigot.getAuthSettings().reload();
                    this.getSettings().getConfig().reload();
                    this.spigotPlugin.getPluginDataStorage().reload();
                    this.spigotPlugin.getTranslationManager().reloadTranslations();
                    SuperAuth.spigot.getBlockActionsListener().onLoad();
                    SuperAuth.spigot.getMainListener().onReload();
                    SuperAuth.spigot.authActionsConfig.reload();
                    this.log("&aReload request sent.");
                    return CommandResult.COMPLETED;
                case "paste":
                    this.executePasteCommand(console.parseConsoleCommandSender());
                    return CommandResult.COMPLETED;
                case "info":
                    this.executeInfoCommand(console.parseConsoleCommandSender());
                    new Thread(()-> {
                        if(!SuperAuth.spigot.isSQLite()){
                            this.log("Testing DataBase Connection");
                            boolean test = SuperAuth.spigot.getDataBase().testConnection();
                            if(test){
                                this.log("&aTest Passed!");
                            }else{
                                this.log("&cTest failed!");
                            }
                        }
                    });
                    return CommandResult.COMPLETED;
                default:
                    return CommandResult.INVALID_ARGS;
            }
        }
        return CommandResult.COMPLETED;
    }
}
