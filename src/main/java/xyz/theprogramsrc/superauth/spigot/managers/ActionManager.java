package xyz.theprogramsrc.superauth.spigot.managers;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterLoginEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterRegisterEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeLoginEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeRegisterEvent;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.memory.CaptchaMemory;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth.spigot.memory.WasRegisteredMemory;
import xyz.theprogramsrc.superauth.spigot.objects.AuthAction;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.placeholders.Placeholder;
import xyz.theprogramsrc.supercoreapi.global.placeholders.SpigotPlaceholderManager;
import xyz.theprogramsrc.supercoreapi.libs.xseries.messages.Titles;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

import java.util.List;

public class ActionManager extends SpigotModule {

    private final Player player;
    private final AuthSettings authSettings;
    private final UserStorage userStorage;
    private final SpigotPlaceholderManager placeholderManager;

    public ActionManager(Player player){
        this.player = player;
        this.placeholderManager = new SpigotPlaceholderManager(this.spigotPlugin);
        this.authSettings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();

        this.placeholderManager.registerPlaceholder(new Placeholder("{Player}", player.getName()));
        this.placeholderManager.registerPlaceholder(new Placeholder("{DisplayName}", player.getDisplayName()));
        this.placeholderManager.registerPlaceholder(new Placeholder("{World}", player.getWorld().getName()));
        this.placeholderManager.registerPlaceholder(new Placeholder("{COORD_X}", player.getLocation().getX()+""));
        this.placeholderManager.registerPlaceholder(new Placeholder("{COORD_Y}", player.getLocation().getY()+""));
        this.placeholderManager.registerPlaceholder(new Placeholder("{COORD_Z}", player.getLocation().getZ()+""));
    }

    public void after(boolean login){
        this.getSpigotTasks().runAsyncTask(() -> {
            if(login){
                this.afterLogin();
            }else{
                this.afterRegister();
            }

            User user = this.userStorage.get(player.getName());
            user.setAuthorized(true);
            this.userStorage.save(user);
            this.getSpigotTasks().runTaskLater(2L, this.player::closeInventory);
        });
    }

    public void before(boolean login){
        this.getSpigotTasks().runTask(() -> {
            if(login){
                this.beforeLogin();
            }else{
                this.beforeRegister();
            }
        });
    }

    private void beforeLogin(){
        SuperAuth.spigot.runEvent(new SuperAuthBeforeLoginEvent(this.authSettings, this.userStorage, this.player.getName()));
        List<String> actions = this.authSettings.getBeforeLogin();
        this.runActions(actions, true, true);
    }

    private void afterLogin(){
        SuperAuth.spigot.runEvent(new SuperAuthAfterLoginEvent(this.authSettings, this.userStorage, this.player.getName()));
        CaptchaMemory.i.remove(this.player.getName());
        List<String> actions = this.authSettings.getAfterLogin();
        this.runActions(actions, false, true);
        int in = this.authSettings.getAfterLoginTitleTimes()[0],
                stay = this.authSettings.getAfterLoginTitleTimes()[1],
                out = this.authSettings.getAfterLoginTitleTimes()[2];
        String title = this.getSuperUtils().color(this.placeholderManager.applyPlaceholders(this.authSettings.getAfterLoginTitle(), this.player)),
                subtitle = this.getSuperUtils().color(this.placeholderManager.applyPlaceholders(this.authSettings.getAfterLoginSubtitle(), this.player));
        this.getSpigotTasks().runTaskLater(20L, () -> Titles.sendTitle(player, in, stay, out, title, subtitle));
        this.getSpigotTasks().runTaskLater(40L, ()-> ForceLoginMemory.i.remove(player.getName()));
    }

    private void beforeRegister(){
        SuperAuth.spigot.runEvent(new SuperAuthBeforeRegisterEvent(this.authSettings, this.userStorage, this.player.getName()));
        List<String> actions = this.authSettings.getBeforeRegister();
        this.runActions(actions, true, false);
    }

    private void afterRegister(){
        SuperAuth.spigot.runEvent(new SuperAuthAfterRegisterEvent(this.authSettings, this.userStorage, this.player.getName()));
        CaptchaMemory.i.remove(player.getName());
        WasRegisteredMemory.i.remove(player.getName());
        List<String> actions = this.authSettings.getAfterRegister();
        this.runActions(actions, false, false);
        int in = this.authSettings.getAfterRegisterTitleTimes()[0],
                stay = this.authSettings.getAfterRegisterTitleTimes()[1],
                out = this.authSettings.getAfterRegisterTitleTimes()[2];
        String title = this.getSuperUtils().color(this.placeholderManager.applyPlaceholders(this.authSettings.getAfterRegisterTitle(), this.player)),
                subtitle = this.getSuperUtils().color(this.placeholderManager.applyPlaceholders(this.authSettings.getAfterRegisterSubtitle(), this.player));
        this.getSpigotTasks().runTaskLater(20L, () -> Titles.sendTitle(player, in, stay, out, title, subtitle));
    }

    private void runActions(List<String> actions, boolean before, boolean login) {
        Thread thread = new Thread(() -> {
            for(String action : actions){
                String[] data = action.split(":", 2);
                String id = data[0];
                AuthAction authAction = AuthAction.fromId(id);
                if(authAction != null){
                    String argument = data[1];
                    if(authAction.canExecute(before, login)){
                        authAction.run(this.placeholderManager.applyPlaceholders(argument, player), player);
                    }
                }
            }
            SuperAuth.actionThreadIds.remove(this.player.getUniqueId());
        });
        thread.setName("SuperAuth Actions " + (before ? "Before" : "After") + " " + (login ? "Login" : "Register") + " - " + this.player.getName());
        SuperAuth.actionThreadIds.put(this.player.getUniqueId(), thread.getId());
        thread.start();
    }
}
