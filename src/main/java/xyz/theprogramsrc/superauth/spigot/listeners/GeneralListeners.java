package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class GeneralListeners extends SpigotModule {

    private UserStorage userStorage;

    @Override
    public void onLoad() {
        this.userStorage = SuperAuth.spigot.getUserStorage();
    }


    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        String username = e.getPlayer().getName();
        this.userStorage.removeCache(username);
    }
}
