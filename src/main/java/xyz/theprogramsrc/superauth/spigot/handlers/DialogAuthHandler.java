package xyz.theprogramsrc.superauth.spigot.handlers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterCaptchaEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeCaptchaEvent;
import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.managers.ActionManager;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialogAuthHandler extends SpigotModule {
    
    private final Player player;
    private final User user;
    private final UserStorage userStorage;
    private final AuthSettings authSettings;
    private final ActionManager actionManager;
    
    public DialogAuthHandler(Player player, User user) {
        this.player = player;
        this.user = user;
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.actionManager = new ActionManager(player);
        this.debug("Loading user '" + player.getName() + "' with " + this.getClass().getSimpleName());
        if(this.user.isRegistered()){ // Execute user login
            actionManager.before(true);
            this.loginDialog();
        }else{ // Execute user registration
            actionManager.before(false);
            this.registerDialog();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public User getUser() {
        return user;
    }

    private void loginDialog(){
        this.getSpigotTasks().runTaskLater(10, ()->{
            final AtomicBoolean success = new AtomicBoolean(false);
            Dialog dialog = new Dialog(player){
                @Override
                public String getTitle() {
                    return LBase.DIALOG_LOGIN_TITLE.toString();
                }

                @Override
                public String getSubtitle() {
                    return LBase.DIALOG_LOGIN_SUBTITLE.toString();
                }

                @Override
                public String getActionbar() {
                    return LBase.DIALOG_LOGIN_ACTIONBAR.toString();
                }

                @Override
                public boolean onResult(String pwd) {
                    try{
                        if(user.isValid(pwd)){
                            success.set(true);
                            return true;
                        }else{
                            this.getSuperUtils().sendMessage(this.getPlayer(), LBase.WRONG_PASSWORD.toString());
                            return false;
                        }
                    }catch (NoSuchAlgorithmException ex){
                        this.plugin.addError(ex);
                        this.getSuperUtils().sendMessage(this.getPlayer(), this.getSettings().getPrefix() + LBase.ERROR_WHILE_HASHING.toString());
                        this.log("&cCouldn't check password:");
                        ex.printStackTrace();
                        return false;
                    }
                }

                @Override
                public boolean canClose() {
                    return ForceLoginMemory.i.has(getPlayer().getName());
                }

                @Override
                public void onDialogClose() {
                    if(success.get()){
                        DialogAuthHandler self = DialogAuthHandler.this;
                        if(self.authSettings.isCaptchaEnabled()){
                            double rand = Utils.random(0.0, 1.0);
                            if(rand >= self.authSettings.getCaptchaChance()){
                                self.captchaDialog(player, false);
                            }else{
                                DialogAuthHandler.this.actionManager.after(true);
                            }
                        }else{
                            DialogAuthHandler.this.actionManager.after(true);
                        }
                    }
                }
            };

            String username = player.getName();
            new BukkitRunnable(){
                @Override
                public void run() {
                    User u = DialogAuthHandler.this.userStorage.get(username);
                    if(u != null){
                        if(!u.isAuthorized()){
                            if(ForceLoginMemory.i.has(username)){
                                DialogAuthHandler.this.getSpigotTasks().runTaskLater(40L, dialog::close);
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimerAsynchronously(this.spigotPlugin, 0L, 20L);
        });
    }

    private void registerDialog(){
        this.getSpigotTasks().runTaskLater(10, ()->{
            final AtomicBoolean success = new AtomicBoolean(false);
            new Dialog(player){
                @Override
                public String getTitle() {
                    return LBase.DIALOG_REGISTER_TITLE.toString();
                }

                @Override
                public String getSubtitle() {
                    return LBase.DIALOG_REGISTER_SUBTITLE.toString();
                }

                @Override
                public String getActionbar() {
                    return LBase.DIALOG_REGISTER_ACTIONBAR.toString();
                }

                @Override
                public boolean onResult(String in) {
                    if(in.startsWith("register") || in.startsWith("/register")){
                        this.getSuperUtils().sendMessage(player, LBase.DIALOG_HOW_TO_USE.toString());
                        return false;
                    }
                    DialogAuthHandler self = DialogAuthHandler.this;

                    if(in.length() < self.authSettings.getMinPasswordLength()){
                        this.getSuperUtils().sendMessage(player, LBase.PASSWORD_TOO_SHORT.options().placeholder("{Length}", self.authSettings.getMinPasswordLength()+"").toString());
                        return false;
                    }

                    if(in.length() > self.authSettings.getMaxPasswordLength()){
                        this.getSuperUtils().sendMessage(player, LBase.PASSWORD_TOO_LONG.options().placeholder("{Length}", self.authSettings.getMaxPasswordLength()+"").toString());
                        return false;
                    }

                    try{
                        String password = Hashing.hash(self.authSettings.getHashingMethod(), in);
                        User u = user.setPassword(password).setAuthMethod("DIALOG").setRegistered(true);
                        self.userStorage.save(u, false);
                        success.set(true);
                        return true;
                    }catch (NoSuchAlgorithmException ex){
                        this.plugin.addError(ex);
                        this.getSuperUtils().sendMessage(this.getPlayer(), this.getSettings().getPrefix() + LBase.ERROR_WHILE_HASHING);
                        this.log("&cCouldn't hash password:");
                        ex.printStackTrace();
                        return false;
                    }
                }

                @Override
                public boolean canClose() {
                    return false;
                }

                @Override
                public void onDialogClose() {
                    if(success.get()){
                        DialogAuthHandler self = DialogAuthHandler.this;
                        if(self.authSettings.isCaptchaEnabled()){
                            double rand = Utils.random(0.0, 1.0);
                            if(rand <= self.authSettings.getCaptchaChance()){
                                self.captchaDialog(player, true);
                            }else{
                                DialogAuthHandler.this.actionManager.after(false);
                            }
                        }else{
                            DialogAuthHandler.this.actionManager.after(false);
                        }
                    }
                }
            };
        });
    }

    private void captchaDialog(final Player player, boolean register){
        this.getSpigotTasks().runTaskLater(10, ()->{
            SuperAuth.spigot.runEvent(new SuperAuthBeforeCaptchaEvent(this.authSettings, this.userStorage, player.getName()));
            final String captcha = Utils.randomAlphaNumeric(this.authSettings.getCaptchaLength());
            Dialog dialog = new Dialog(player){
                @Override
                public String getTitle() {
                    return LBase.DIALOG_CAPTCHA_TITLE.toString();
                }

                @Override
                public String getSubtitle() {
                    return LBase.DIALOG_CAPTCHA_SUBTITLE.options().vars(captcha).placeholder("{Captcha}", captcha).toString(); // Remove var in v3.17
                }

                @Override
                public String getActionbar() {
                    return LBase.DIALOG_CAPTCHA_ACTIONBAR.options().vars(captcha).placeholder("{Captcha}", captcha).toString(); // Remove var in v3.17
                }

                @Override
                public boolean onResult(String playerInput) {
                    if(!playerInput.contentEquals(captcha)){
                        this.getSuperUtils().sendMessage(player, LBase.WRONG_CAPTCHA.options().vars(captcha).placeholder("{Captcha}", captcha).toString()); // Remove var in v3.17
                        return false;
                    }else{
                        SuperAuth.spigot.runEvent(new SuperAuthAfterCaptchaEvent(DialogAuthHandler.this.authSettings, DialogAuthHandler.this.userStorage, player.getName()));
                        DialogAuthHandler.this.actionManager.after(!register);
                        return true;
                    }
                }

                @Override
                public boolean canClose() {
                    return ForceLoginMemory.i.has(getPlayer().getName());
                }
            };

            String username = player.getName();

            new BukkitRunnable(){
                @Override
                public void run() {
                    User u = DialogAuthHandler.this.userStorage.get(username);
                    if(u != null){
                        if(!u.isAuthorized()){
                            if(ForceLoginMemory.i.has(username)){
                                DialogAuthHandler.this.getSpigotTasks().runTaskLater(40L, dialog::close);
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimerAsynchronously(this.spigotPlugin, 0L, 20L);
        });
    }
}
