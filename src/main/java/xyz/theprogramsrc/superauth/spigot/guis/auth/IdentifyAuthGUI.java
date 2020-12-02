package xyz.theprogramsrc.superauth.spigot.guis.auth;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.global.users.User;

public class IdentifyAuthGUI {

    public IdentifyAuthGUI(Player player, User user, boolean registering){
        if(registering){
            new RegisterGUI(player, user);
        }else{
            new LoginGUI(player, user);
        }
    }

}
