package xyz.theprogramsrc.superauth.spigot.storage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class DatabaseMigration extends SpigotModule {

    public static boolean migrating = false;
    private final DataBase from, to;

    public DatabaseMigration(DataBase from, DataBase to){
        this.from = from;
        this.to = to;
    }

    public void init(Consumer<Boolean> then){
        this.log("&cCollecting data...");
        UserStorage usFrom = new UserStorage(plugin, this.from),
                usTo = new UserStorage(plugin, this.to);
        spigotPlugin.getServer().getOnlinePlayers().forEach(p-> p.kickPlayer(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later.")));
        this.log("&cInitializing migration...");
        migrating = true;
        usFrom.requestUsers(true, users -> {
            AtomicInteger errors = new AtomicInteger(0);
            usTo.getDataBase().connect(c-> {
                for (User user : users) {
                    usTo.saveUser(user, c, null, e -> {
                        this.log("&cFailed to save migrate user '" + user.getUsername() + "':");
                        e.printStackTrace();
                        errors.getAndIncrement();
                    });
                }
            });
            this.log("&cMigration finished");
            then.accept(errors.get() == 0);
        });
    }

    public void revert(){ // Here we remove the data from the new database :)
        UserStorage userStorage = new UserStorage(plugin, this.to);
        userStorage.requestUsers(true, users -> {
            for (User user : users) {
                userStorage.remove(user, null);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        if(migrating){
            e.getPlayer().kickPlayer(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later."));
        }
    }
}
