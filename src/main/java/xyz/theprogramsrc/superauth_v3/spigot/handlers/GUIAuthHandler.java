package xyz.theprogramsrc.superauth_v3.spigot.handlers;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.spigot.guis.auth.IdentifyAuthGUI;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

public class GUIAuthHandler extends SpigotModule {

    public GUIAuthHandler(Player player, User user) {
        this.debug("Loading user '" + player.getName() + "' with " + this.getClass().getSimpleName());
        new IdentifyAuthGUI(player, user);
    }
}
