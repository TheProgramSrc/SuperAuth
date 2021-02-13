package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterCaptchaEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeCaptchaEvent;
import xyz.theprogramsrc.superauth.global.hashing.Hashing;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.guis.auth.IdentifyAuthGUI;
import xyz.theprogramsrc.superauth.spigot.memory.CaptchaMemory;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.spigot.dialog.Dialog;
import xyz.theprogramsrc.supercoreapi.spigot.utils.skintexture.SkinTexture;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

import static xyz.theprogramsrc.superauth.spigot.objects.AuthMethod.*;

public class JoinListener extends SpigotModule {

    private UserStorage userStorage;
    private AuthSettings settings;
    
    @Override
    public void onLoad() {
        this.userStorage = SuperAuth.spigot.getUserStorage();
        this.settings = SuperAuth.spigot.getAuthSettings();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(final PlayerJoinEvent event){
        new Thread(() -> this.handleAuth(event.getPlayer(), true)).start();
    }

    public void onReload(){
        for(Player player : Bukkit.getOnlinePlayers()){
            new Thread(() -> this.handleAuth(player, false)).start();
        }
    }

    private void handleAuth(Player player, boolean disableAuthorization){
        try{
            this.getSpigotTasks().runTaskLater(10, ()->{
                User user = this.userStorage.get(player.getName());
                if(user == null) return;
                if(user.isAuthorized()){
                    if(!disableAuthorization) return;
                    user.setAuthorized(false);
                    this.userStorage.save(user);
                    user = this.userStorage.get(player.getName());
                }
                this.checkSkin(user, player);
                if(!this.settings.isAuthEnabled()) return;
                this.executeAntiBot(player, user);
                this.beforeAuth(player, user, !user.isRegistered());
                if(!user.isRegistered()){
                    if(this.settings.getAuthMethod().equals(DIALOG)){
                        this.registerDialog(user, player);
                    }else if(this.settings.getAuthMethod().equals(GUI)){
                        new IdentifyAuthGUI(player, user, true);
                    }else{
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                User finalUser = JoinListener.this.userStorage.get(player.getName());
                                if(finalUser != null){
                                    if(!finalUser.isAuthorized()){
                                        if(!CaptchaMemory.i.has(player.getName())) {
                                            JoinListener.this.getSuperUtils().sendMessage(player, LBase.REGISTER_COMMAND_USAGE.toString());
                                        }else{
                                            this.cancel();
                                        }
                                    }else{
                                        this.cancel();
                                    }
                                }
                            }
                        }.runTaskTimer(this.spigotPlugin, 0L, Utils.toTicks(this.settings.getCommandUsageTimer()));
                    }
                }else{
                    if(user.isPremium() && this.settings.getPremiumAutoLogin()){
                        SuperAuth.spigot.afterRegister(player);
                    }else{
                        if(user.getAuthMethod().equals("DIALOG")){
                            this.loginDialog(user, player);
                        }else if(user.getAuthMethod().equals("GUI")){
                            new IdentifyAuthGUI(player, user, false);
                        }else{
                            user.setAuthorized(false);
                            this.userStorage.save(user);
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    User finalUser = JoinListener.this.userStorage.get(player.getName());
                                    if(finalUser != null){
                                        if(!finalUser.isAuthorized()){
                                            if(!CaptchaMemory.i.has(player.getName())) {
                                                JoinListener.this.getSuperUtils().sendMessage(player, LBase.LOGIN_COMMAND_USAGE.toString());
                                            }else{
                                                this.cancel();
                                            }
                                        }else{
                                            this.cancel();
                                        }
                                    }
                                }
                            }.runTaskTimer(this.spigotPlugin, 0L, Utils.toTicks(this.settings.getCommandUsageTimer()));
                        }
                    }
                }
            });
        }catch (Exception e){
            this.plugin.addError(e);
            e.printStackTrace();
        }
    }

    private void loginDialog(final User user, final Player player){
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
                        JoinListener self = JoinListener.this;
                        if(self.settings.isCaptchaEnabled()){
                            double rand = Utils.random(0.0, 1.0);
                            if(rand >= self.settings.getCaptchaChance()){
                                self.captchaDialog(player, false);
                            }else{
                                SuperAuth.spigot.afterLogin(player);
                            }
                        }else{
                            SuperAuth.spigot.afterLogin(player);
                        }
                    }
                }
            };

            String username = player.getName();

            new BukkitRunnable(){
                @Override
                public void run() {
                    User u = JoinListener.this.userStorage.get(username);
                    if(u != null){
                        if(!u.isAuthorized()){
                            if(ForceLoginMemory.i.has(username)){
                                JoinListener.this.getSpigotTasks().runTaskLater(40L, dialog::close);
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(this.spigotPlugin, 0L, 20L);
        });
    }

    private void registerDialog(final User user, final Player player){
        this.getSpigotTasks().runTaskLater(10, ()->{
            final AtomicBoolean success = new AtomicBoolean(false);
            Dialog dialog = new Dialog(player){
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
                    JoinListener self = JoinListener.this;

                    if(in.length() < self.settings.getMinPasswordLength()){
                        this.getSuperUtils().sendMessage(player, LBase.PASSWORD_TOO_SHORT.options().placeholder("{Length}", self.settings.getMinPasswordLength()+"").toString());
                        return false;
                    }

                    if(in.length() > self.settings.getMaxPasswordLength()){
                        this.getSuperUtils().sendMessage(player, LBase.PASSWORD_TOO_LONG.options().placeholder("{Length}", self.settings.getMaxPasswordLength()+"").toString());
                        return false;
                    }

                    try{
                        String password = Hashing.hash(JoinListener.this.settings.getHashingMethod(), in);
                        user.setPassword(password).setAuthMethod("DIALOG").setRegistered(true);
                        self.userStorage.save(user);
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
                        JoinListener self = JoinListener.this;
                        if(self.settings.isCaptchaEnabled()){
                            double rand = Utils.random(0.0, 1.0);
                            if(rand <= self.settings.getCaptchaChance()){
                                self.captchaDialog(player, true);
                            }else{
                                SuperAuth.spigot.afterRegister(player);
                            }
                        }else{
                            SuperAuth.spigot.afterRegister(player);
                        }
                    }
                }
            };
        });
    }

    private void captchaDialog(final Player player, boolean register){
        this.getSpigotTasks().runTaskLater(10, ()->{
            SuperAuth.spigot.runEvent(new SuperAuthBeforeCaptchaEvent(this.settings, this.userStorage, player.getName()));
            final String captcha = Utils.randomAlphaNumeric(this.settings.getCaptchaLength());
            Dialog dialog = new Dialog(player){
                @Override
                public String getTitle() {
                    return LBase.DIALOG_CAPTCHA_TITLE.toString();
                }

                @Override
                public String getSubtitle() {
                    return LBase.DIALOG_CAPTCHA_SUBTITLE.options().vars(captcha).toString();
                }

                @Override
                public String getActionbar() {
                    return LBase.DIALOG_CAPTCHA_ACTIONBAR.options().vars(captcha).toString();
                }

                @Override
                public boolean onResult(String playerInput) {
                    if(!playerInput.contentEquals(captcha)){
                        this.getSuperUtils().sendMessage(player, LBase.WRONG_CAPTCHA.options().vars(captcha).toString());
                        return false;
                    }else{
                        SuperAuth.spigot.runEvent(new SuperAuthAfterCaptchaEvent(JoinListener.this.settings, JoinListener.this.userStorage, player.getName()));
                        if(register){
                            SuperAuth.spigot.afterRegister(player);
                        }else{
                            SuperAuth.spigot.afterLogin(player);
                        }
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
                    User u = JoinListener.this.userStorage.get(username);
                    if(u != null){
                        if(!u.isAuthorized()){
                            if(ForceLoginMemory.i.has(username)){
                                JoinListener.this.getSpigotTasks().runTaskLater(40L, dialog::close);
                                this.cancel();
                            }
                        }else{
                            this.cancel();
                        }
                    }
                }
            }.runTaskTimer(this.spigotPlugin, 0L, 20L);
        });
    }

    private void beforeAuth(Player player, User user, boolean registering){
        if(registering){
            if(this.settings.getAuthMethod() == COMMANDS){
                this.getSuperUtils().sendMessage(player, LBase.COMMAND_HOW_TO_USE.options().vars(this.settings.getRegisterCommand().toLowerCase(), this.settings.getLoginCommand().toLowerCase()).toString());
            }else if(this.settings.getAuthMethod() == GUI){
                this.getSuperUtils().sendMessage(player, LBase.GUI_HOW_TO_USE.options().vars(this.settings.getAuthCommand().toLowerCase()).toString());
            }else{
                this.getSuperUtils().sendMessage(player, LBase.DIALOG_HOW_TO_USE.toString());
            }
            SuperAuth.spigot.beforeRegister(player);
        }else{
            if(user.getAuthMethod().equals("COMMANDS")){
                this.getSuperUtils().sendMessage(player, LBase.COMMAND_HOW_TO_USE.options().vars(this.settings.getRegisterCommand().toLowerCase(), this.settings.getLoginCommand().toLowerCase()).toString());
            }else if(user.getAuthMethod().equals("GUI")){
                this.getSuperUtils().sendMessage(player, LBase.GUI_HOW_TO_USE.options().vars(this.settings.getAuthCommand().toLowerCase()).toString());
            }else{
                this.getSuperUtils().sendMessage(player, LBase.DIALOG_HOW_TO_USE.toString());
            }
            SuperAuth.spigot.beforeLogin(player);
        }
    }

    private void executeAntiBot(final Player player, User user){
        int max = this.settings.getMaxTime();
        this.getSpigotTasks().runTaskLater(Utils.toTicks(max), ()->{
            User currentUser = this.userStorage.get(user.getUsername());
            if(currentUser != null){
                if(!currentUser.isAuthorized()){
                    player.closeInventory();
                    player.kickPlayer(this.getSuperUtils().color(LBase.TOOK_TOO_LONG.options().vars(max+"").toString()));
                }
            }
        });
    }

    private void checkSkin(User user, Player player){
        if(Utils.isConnected()){
            if(user == null) return;
            if(!user.hasSkin()){
                SkinTexture skin = this.spigotPlugin.getSkinManager().getSkin(player);
                if(skin == null)
                    return;
                user.setSkinTexture(skin.toString());
                this.userStorage.save(user);
            }
        }
    }
}
