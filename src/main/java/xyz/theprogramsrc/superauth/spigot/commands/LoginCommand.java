package xyz.theprogramsrc.superauth.spigot.commands;

import java.security.NoSuchAlgorithmException;

import org.bukkit.entity.Player;

import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterCaptchaEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeCaptchaEvent;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.CaptchaMemory;
import xyz.theprogramsrc.superauth.spigot.memory.WasRegisteredMemory;
import xyz.theprogramsrc.superauth.spigot.objects.AuthMethod;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.commands.CommandResult;
import xyz.theprogramsrc.supercoreapi.spigot.commands.SpigotCommand;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;
import xyz.theprogramsrc.supercoreapi.spigot.utils.SpigotConsole;

public class LoginCommand extends SpigotCommand {

    private final UserStorage userStorage;
    private final AuthSettings authSettings;

    public LoginCommand(){
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();
    }

    @Override
    public String getCommand() {
        return SuperAuth.spigot.getAuthSettings().getLoginCommand().toLowerCase();
    }

    @Override
    public String[] getAliases() {
        return Utils.toStringArray(SuperAuth.spigot.getAuthSettings().getLoginAliases());
    }

    @Override
    public CommandResult onPlayerExecute(Player player, String[] args) {
        this.getSpigotTasks().runAsyncTask(() -> {
            this.userStorage.get(player.getName(), true, user -> {
                if(user == null){
                    this.getSuperUtils().sendMessage(player, this.getSettings().getPrefix() + LBase.ERROR_FETCHING_DATA);
                }else{
                    if(this.authSettings.getAuthMethod() != AuthMethod.COMMANDS){
                        if(user.isRegistered()){
                            if(user.getAuthMethod().equals("COMMANDS")){
                                if(user.isAuthorized()){
                                    this.getSuperUtils().sendMessage(player, LBase.ALREADY_IDENTIFIED.toString());
                                }else{
                                    this.exe(user, player, args);
                                }
                            }
                        }
                    }else{
                        if(!user.isRegistered()){
                            String cmd = SuperAuth.spigot.getAuthSettings().getRegisterCommand().toLowerCase(); // Remove var in v3.17
                            this.getSuperUtils().sendMessage(player, LBase.USE_REGISTER_COMMAND.options().vars(cmd).placeholder("{Command}", cmd).toString());
                        }else{
                            if(user.getAuthMethod().equals("COMMANDS")){
                                if(user.isAuthorized()){
                                    this.getSuperUtils().sendMessage(player, LBase.ALREADY_IDENTIFIED.toString());
                                }else{
                                    this.exe(user, player, args);
                                }
                            }
                        }
                    }
                }
            });
        });
        return CommandResult.COMPLETED;
    }

    @Override
    public CommandResult onConsoleExecute(SpigotConsole console, String[] args) {
        return CommandResult.NOT_SUPPORTED;
    }

    private void exe(User user, Player player, String[] args){
        try{
            if(args.length == 0) return;

            if(!user.isValid(args[0])){
                this.getSuperUtils().sendMessage(player, LBase.WRONG_PASSWORD.toString());
            }else{
                if(this.authSettings.isCaptchaEnabled()){
                    if(Utils.random(0.0, 1.0) <= this.authSettings.getCaptchaChance()){
                        SuperAuth.spigot.runEvent(new SuperAuthBeforeCaptchaEvent(this.authSettings, this.userStorage, player.getName()));
                        WasRegisteredMemory.i.add(player.getName(), true);
                        CaptchaMemory.i.add(player.getName(), user);
                        final String captcha = Utils.randomAlphaNumeric(this.authSettings.getCaptchaLength());
                        new Dialog(player){
                            @Override
                            public String getTitle() {
                                return LBase.DIALOG_CAPTCHA_TITLE.toString();
                            }

                            @Override
                            public String getSubtitle() {
                                return LBase.DIALOG_CAPTCHA_SUBTITLE.options().placeholder("{Captcha}", captcha).toString();
                            }

                            @Override
                            public String getActionbar() {
                                return LBase.DIALOG_CAPTCHA_ACTIONBAR.options().placeholder("{Captcha}", captcha).toString();
                            }

                            @Override
                            public boolean onResult(String playerInput) {
                                if(!playerInput.contentEquals(captcha)){
                                    this.getSuperUtils().sendMessage(player, LBase.WRONG_CAPTCHA.options().placeholder("{Captcha}", captcha).toString());
                                    return false;
                                }else{
                                    SuperAuth.spigot.runEvent(new SuperAuthAfterCaptchaEvent(LoginCommand.this.authSettings, LoginCommand.this.userStorage, player.getName()));
                                    new ActionManager(this.getPlayer()).after(true);
                                    return true;
                                }
                            }

                            @Override
                            public boolean canClose() {
                                return false;
                            }
                        };
                    }else{
                        new ActionManager(player).after(true);
                    }
                }else{
                    new ActionManager(player).after(true);
                }
            }
        }catch (NoSuchAlgorithmException ex){
            this.plugin.addError(ex);
            this.getSuperUtils().sendMessage(player, LBase.ERROR_WHILE_HASHING.toString());
            this.log("&c" + LBase.ERROR_WHILE_HASHING_PASSWORD);
            ex.printStackTrace();
        }
    }
}
