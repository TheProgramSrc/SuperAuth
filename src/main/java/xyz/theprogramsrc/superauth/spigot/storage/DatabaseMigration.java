package xyz.theprogramsrc.superauth.spigot.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

import java.sql.SQLException;

public class DatabaseMigration extends SpigotModule {

    private boolean migrating;
    private final DataBase from, to;

    public DatabaseMigration(DataBase from, DataBase to){
        this.from = from;
        this.to = to;
    }

    public void init(){
        this.log("&cCollecting data...");
        UserStorage usFrom = new UserStorage(plugin, this.from),
                usTo = new UserStorage(plugin, this.to);
        spigotPlugin.getServer().getOnlinePlayers().forEach(p-> p.kickPlayer(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later.")));
        this.log("&cInitializing migration...");
        this.migrating = true;
        final User[] users = usFrom.requestUsers(true);
        usTo.getDataBase().connect(c-> {
            for (User user : users) {
                try{
                    usTo.saveUser(user, c);
                }catch (SQLException e){
                    this.log("&cFailed to save migrate user '" + user.getUsername() + "':");
                    e.printStackTrace();
                }
            }
        });
        this.log("&cMigration finished");
        this.migrating = false;
    }

    public boolean isMigrating(){
        return this.migrating;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e){
        if(this.migrating){
            e.getPlayer().kickPlayer(this.getSuperUtils().color("&bSuperAuth &cis currently migrating its data. Please try again later."));
        }
    }
}
