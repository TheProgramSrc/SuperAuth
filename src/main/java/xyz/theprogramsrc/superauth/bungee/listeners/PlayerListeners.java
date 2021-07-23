package xyz.theprogramsrc.superauth.bungee.listeners;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.event.EventHandler;
import xyz.theprogramsrc.superauth.bungee.SuperAuth;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.global.vpn_blocker.VPNBlocker;
import xyz.theprogramsrc.supercoreapi.bungee.BungeeModule;
import xyz.theprogramsrc.supercoreapi.global.utils.ServerUtils;

public class PlayerListeners extends BungeeModule {

    private UserStorage userStorage;
    private VPNBlocker vpnBlocker;
    private String authServer;
    private ServerUtils serverUtils;

    @Override
    public void onLoad() {
        this.serverUtils = SuperAuth.bungee.getServerUtils();
        this.vpnBlocker = SuperAuth.bungee.getVPNBlocker();
        this.userStorage = SuperAuth.bungee.getUserStorage();
        this.authServer = SuperAuth.bungee.getAuthServer();
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event){
        PendingConnection connection = event.getConnection();
        String username = connection.getName();
        if(this.userStorage.exists(username)){
            User user = this.userStorage.get(username, true);
            if(user != null){
                if(user.isAuthorized()) {
                    user.setAuthorized(false);
                    this.userStorage.save(user);
                }
                connection.setOnlineMode(user.isPremium());
            }
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event){
        PendingConnection connection = event.getConnection();
        String username = connection.getName();
        String ip = null;
        if(connection.getVirtualHost() != null && connection.getVirtualHost().getAddress() != null){
            ip = connection.getVirtualHost().getAddress().getHostAddress();
            if(this.vpnBlocker.isVPN(ip)){
                event.setCancelReason(new TextComponent(this.getSuperUtils().color(LBase.VPN_KICK.toString())));
                event.setCancelled(true);
                return;
            }
        }

        User user;
        if(this.userStorage.exists(username)){
            user = this.userStorage.get(username, true);
            if(user != null){
                if(user.isAuthorized()) {
                    user.setAuthorized(false);
                    if ((user.getIp() == null || user.getIp().equals("") || user.getIp().equals(" ") || user.getIp().equals("null")) && ip != null) {
                        user.setIp(ip);
                    }

                    this.userStorage.save(user);
                }
            }
        }else{
            user = new User(username)
                    .setAuthorized(false);
            if(ip != null)
                user.setIp(ip);
            this.userStorage.save(user);
        }
    }

    @EventHandler
    public void onConnect(ServerConnectEvent event){
        ProxiedPlayer player = event.getPlayer();
        if(player == null)
            return;
        User user = this.userStorage.get(player.getName());
        if(user == null)
            return;
        if(!user.isAuthorized()){
            Server server = player.getServer();
            if(server == null)
                return;
            ServerInfo info = server.getInfo();
            if(info == null)
                return;
            if(info.getName() == null)
                return;
            if(!info.getName().equals(this.authServer)){
                this.log("The user '"+user.getUsername()+"' didn't completed successfully the authentication so it was sent to the AuthServer");
                this.serverUtils.bungee().sendToServer(player, this.authServer);
            }
        }
    }
}
