package xyz.theprogramsrc.superauth.spigot.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseMigration extends SpigotModule {

    public static boolean migrating = false;
    private final DataBase from, to;

    public DatabaseMigration(DataBase from, DataBase to){
        this.from = from;
        this.to = to;
    }

    public boolean init(){
        this.log("&cCollecting data...");
        UserStorage usFrom = new UserStorage(plugin, this.from),
                usTo = new UserStorage(plugin, this.to);
        spigotPlugin.getServer().getOnlinePlayers().forEach(p-> p.kickPlayer(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later.")));
        this.log("&cInitializing migration...");
        migrating = true;
        final User[] users = usFrom.requestUsers(true);
        AtomicInteger errors = new AtomicInteger(0);
        usTo.getDataBase().connect(c-> {
            for (User user : users) {
                try{
                    usTo.saveUser(user, c);
                }catch (SQLException e){
                    this.log("&cFailed to save migrate user '" + user.getUsername() + "':");
                    e.printStackTrace();
                    errors.getAndIncrement();
                }
            }
        });
        this.log("&cMigration finished");
        return errors.get() == 0;
    }

    public void revert(){ // Here we remove the data from the new database :)
        UserStorage userStorage = new UserStorage(plugin, this.to);
        for (User user : userStorage.requestUsers(true)) {
            userStorage.remove(user);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        if(migrating){
            e.getPlayer().kickPlayer(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later."));
        }
    }
}
