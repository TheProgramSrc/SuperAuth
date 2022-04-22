package xyz.theprogramsrc.superauth_v3.spigot.guis.auth;

import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.spigot.managers.ActionManager;

public class IdentifyAuthGUI {

    public IdentifyAuthGUI(Player player, User user){
        ActionManager actionManager = new ActionManager(player);
        if(!user.isRegistered()){
            actionManager.before(false);
            new RegisterGUI(player, user);
        }else{
            actionManager.before(true);
            new LoginGUI(player, user);
        }
    }

}
