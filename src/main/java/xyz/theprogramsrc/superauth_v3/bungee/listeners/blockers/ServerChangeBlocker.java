package xyz.theprogramsrc.superauth_v3.bungee.listeners.blockers;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.event.EventHandler;
import xyz.theprogramsrc.superauth_v3.bungee.SuperAuth;
import xyz.theprogramsrc.superauth_v3.global.languages.LBase;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.supercoreapi.bungee.BungeeModule;
import xyz.theprogramsrc.supercoreapi.global.utils.ServerUtils;

public class ServerChangeBlocker extends BungeeModule {

    private UserStorage userStorage;
    private ServerUtils serverUtils;

    @Override
    public void onLoad() {
        this.serverUtils = SuperAuth.bungee.getServerUtils();
        this.userStorage = SuperAuth.bungee.getUserStorage();
    }

    @EventHandler
    public void onServerChange(ServerConnectedEvent event){
        ProxiedPlayer player = event.getPlayer();
        this.getBungeeTasks().runAsync(() -> {
            this.userStorage.get(player.getName(), true, user -> {
                if(user != null){
                    if(!user.isAuthorized()){
                        String authServer = SuperAuth.bungee.getAuthServer();
                        if(!event.getServer().getInfo().getName().equalsIgnoreCase(authServer)){
                            this.serverUtils.bungee().sendToServer(player, authServer);
                            this.getSuperUtils().sendMessage(player, LBase.STILL_IN_AUTH.toString());
                        }
                    }
                }
            });
        });
    }
}
