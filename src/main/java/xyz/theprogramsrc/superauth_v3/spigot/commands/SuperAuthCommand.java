package xyz.theprogramsrc.superauth_v3.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.superauth_v3.spigot.guis.account.MyAccountGUI;
import xyz.theprogramsrc.superauth_v3.spigot.guis.admin.AdminGUI;
import xyz.theprogramsrc.superauth_v3.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth_v3.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.supercoreapi.global.translations.Base;
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
        return new String[]{"superauth_v3"};
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] args) {
        this.userStorage.get(player.getName(), user -> {
            if(args.length == 0){
                if(user.isAdmin()){
                    new AdminGUI(player);
                }else{
                    if(!player.hasPermission("superauth_v3.my-account")){
                        this.getSuperUtils().sendMessage(player, "&c" + Base.NO_PERMISSION);
                    }else{
                        new MyAccountGUI(player);
                    }
                }
            }else{
                if(args[0].equalsIgnoreCase("info")){
                    if(user.isAdmin()){
                        this.executeInfoCommand(player);
                        this.getSpigotTasks().runAsyncTask(() -> {
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
                    }
                }else if(args[0].equalsIgnoreCase("paste")){
                    if(user.isAdmin()){
                        this.executePasteCommand(player);
                    }
                }
            }
        });
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
                        this.getSpigotTasks().runAsyncTask(() -> {
                            this.userStorage.exists(username, exists -> {
                                if(!exists){
                                    this.log("&c" + LBase.USER_NOT_EXISTS);
                                }else{
                                    this.userStorage.get(username, user -> {
                                        if (!user.isRegistered()) {
                                            this.log("&c" + LBase.USER_NOT_REGISTERED);
                                        } else {
                                            String val = args[3];
                                            switch (args[2].toLowerCase()){
                                                case "ip":
                                                    user.setIp(val);
                                                    this.getSpigotTasks().runAsyncTask(() -> {
                                                        this.userStorage.save(user, () -> {
                                                            this.log("&a" + LBase.CONSOLE_UPDATED_USER_IP_ADDRESS.options().placeholder("{NewIPAddress}", val).placeholder("{UserName}", user.getUsername()));
                                                        });
                                                    });
                                                    break;
                                                case "premium":
                                                    user.setPremium(val.equalsIgnoreCase("true"));
                                                    boolean mode = user.isPremium();
                                                    this.getSpigotTasks().runAsyncTask(() -> {
                                                        this.userStorage.save(user, () -> {
                                                            this.log("&a" + LBase.CONSOLE_UPDATED_USER_MODE.options().placeholder("{NewMode}", (mode ? LBase.PREMIUM : LBase.CRACKED).toString()).placeholder("{UserName}", user.getUsername()));
                                                        });
                                                    });
                                                    break;
                                                default:
                                                    this.log("&c" + Base.INVALID_ARGUMENTS);
                                                    break;
                                            }
                                        }
                                    });
                                }
                            });
                        });
                    }
                    break;
                case "setadmin":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        this.getSpigotTasks().runAsyncTask(() -> {
                            this.userStorage.exists(username, exists -> {
                                if (!exists) {
                                    this.log("&c" + LBase.USER_NOT_EXISTS);
                                } else {
                                    this.userStorage.get(username, user -> {
                                        if (!user.isRegistered()) {
                                            this.log("&c" + LBase.USER_NOT_REGISTERED);
                                        } else {
                                            if (user.isAdmin()) {
                                                this.log(LBase.ALREADY_ADMIN.options().placeholder("{User}", user.getUsername()).get());
                                            } else {
                                                user.setAdmin(true);
                                                this.userStorage.save(user);
                                                this.log(LBase.ADDED_ADMIN.options().placeholder("{User}", user.getUsername()).get());
                                            }
                                        }
                                    });
                                }
                            });
                        });
                    }
                    break;
                case "remadmin":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        this.getSpigotTasks().runAsyncTask(() -> {
                            this.userStorage.exists(username, exists -> {
                                if (!exists) {
                                    this.log("&c" + LBase.USER_NOT_EXISTS);
                                } else {
                                    this.userStorage.get(username, user -> {
                                        if (!user.isRegistered()) {
                                            this.log("&c" + LBase.USER_NOT_REGISTERED);
                                        } else {
                                            if (!user.isAdmin()) {
                                                this.log(LBase.ALREADY_NON_ADMIN.options().placeholder("{User}", user.getUsername()).get());
                                            } else {
                                                user.setAdmin(false);
                                                this.userStorage.save(user);
                                                this.log(LBase.REMOVED_ADMIN.options().placeholder("{User}", user.getUsername()).get());
                                            }
                                        }
                                    });
                                }
                            });
                        });
                    }
                    break;
                case "force-login":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        this.getSpigotTasks().runAsyncTask(() -> {
                            this.userStorage.exists(username, exists -> {
                                if (!exists) {
                                    this.log("&c" + LBase.USER_NOT_EXISTS);
                                } else {
                                    this.userStorage.get(username, user -> {
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
                                                        this.log("&a" + LBase.FORCED_LOGIN.options().placeholder("{User}", user.getUsername()).get());
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        });
                    }
                    break;
                case "unregister":
                    if (args.length == 1) {
                        return CommandResult.INVALID_ARGS;
                    } else {
                        String username = args[1];
                        this.getSpigotTasks().runAsyncTask(() -> {
                            this.userStorage.exists(username, exists -> {
                                if (!exists) {
                                    this.log("&c" + LBase.USER_NOT_EXISTS);
                                } else {
                                    this.userStorage.get(username, user -> {
                                        if (!user.isRegistered()) {
                                            this.log("&c" + LBase.USER_NOT_REGISTERED);
                                        } else {
                                            this.log("&a" + LBase.REMOVE_REQUEST_SENT);
                                            this.userStorage.remove(user, () -> {
                                                this.getSpigotTasks().runTask(() -> {
                                                    Player p = Bukkit.getPlayer(username);
                                                    if(p != null){
                                                        if(p.isOnline()){
                                                            p.kickPlayer("Disconnected");
                                                        }
                                                    }
                                                });
                                            });
                                        }
                                    });
                                }
                            });
                        });
                    }
                    break;
                case "reload":
                    SuperAuth.spigot.getAuthSettings().load();
                    this.getSettings().getConfig().load();
                    this.spigotPlugin.getPluginDataStorage().reload();
                    this.spigotPlugin.getTranslationManager().loadTranslations();
                    SuperAuth.spigot.getBlockActionsListener().onLoad();
                    SuperAuth.spigot.getMainListener().onReload();
                    SuperAuth.spigot.authActionsConfig.load();
                    this.log("&aReload request sent.");
                    return CommandResult.COMPLETED;
                case "paste":
                    this.executePasteCommand(console.parseConsoleCommandSender());
                    return CommandResult.COMPLETED;
                case "info":
                    this.executeInfoCommand(console.parseConsoleCommandSender());
                    this.getSpigotTasks().runAsyncTask(() -> {
                        if(!SuperAuth.spigot.isSQLite()){
                            this.log("&7Testing connection with MySQL DataBase...");
                            boolean test = SuperAuth.spigot.getDataBase().testConnection();
                            if(test){
                                this.log("&aTest Passed!");
                            }else{
                                this.log("&cTest failed!");
                            }
                        }else{
                            this.log("&7The plugin is currently connected to SQLite");
                        }
                    });
                    return CommandResult.COMPLETED;
                case "migrate":
                    SuperAuth.spigot.migrateBetweenDatabases();
                    return CommandResult.COMPLETED;
                default:
                    return CommandResult.INVALID_ARGS;
            }
        }
        return CommandResult.COMPLETED;
    }
}
