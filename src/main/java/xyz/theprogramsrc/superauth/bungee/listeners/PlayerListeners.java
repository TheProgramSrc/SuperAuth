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
    public void onPreLogin(PreLoginEvent event) {
        if (event.isCancelled())
            return;
        PendingConnection connection = event.getConnection();
        if (connection == null)
            return;
        if (!connection.isConnected())
            return;
        String username = connection.getName();
        if (username == null)
            return;
        event.registerIntent(this.bungeePlugin);
        this.getBungeeTasks().runAsync(() -> this.userStorage.exists(username, exists -> {
            if (exists.booleanValue()) {
                this.userStorage.get(username, user -> {
                    if (user != null) {
                        connection.setOnlineMode(user.isPremium());
                        event.completeIntent(this.bungeePlugin);
                    }
                });
            }
        }, ex -> {
            if (!ex.getMessage().toLowerCase().contains("timed out")) {
                ex.printStackTrace();
            }
        }));
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        PendingConnection connection = event.getConnection();
        String username = connection.getName();
        String ip = null;
        if (connection.getVirtualHost() != null && connection.getVirtualHost().getAddress() != null) {
            ip = connection.getVirtualHost().getAddress().getHostAddress();
            if (this.vpnBlocker.isVPN(ip)) {
                event.setCancelReason(new TextComponent(this.getSuperUtils().color(LBase.VPN_KICK.toString())));
                event.setCancelled(true);
                return;
            }
        }
        String finalIp = ip;
        this.getBungeeTasks().runAsync(() -> this.userStorage.exists(username, exists -> {
            if (exists.booleanValue()) {
                this.userStorage.get(username, true, user -> {
                    if (user != null && user.isAuthorized()) {
                        user.setAuthorized(false);
                        if ((user.getIp() == null || user.getIp().equals("") || user.getIp().equals(" ")
                                || user.getIp().equals("null")) && finalIp != null) {
                            user.setIp(finalIp);
                        }

                        this.userStorage.save(user);
                    }
                });
            } else {
                User user = new User(username).setAuthorized(false);
                if (finalIp != null) {
                    user.setIp(finalIp);
                }
                this.userStorage.save(user);
            }
        }, ex -> {
            if (!ex.getMessage().toLowerCase().contains("timed out")) {
                ex.printStackTrace();
            }
        }));
    }

    @EventHandler
    public void onConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player == null)
            return;
        this.getBungeeTasks().runAsync(() -> this.userStorage.get(player.getName(), user -> {
            if (user == null)
                return;
            if (!user.isAuthorized()) {
                Server server = player.getServer();
                if (server == null)
                    return;
                ServerInfo info = server.getInfo();
                if (info == null)
                    return;
                if (info.getName() == null)
                    return;
                if (!info.getName().equals(this.authServer)) {
                    this.log("The user '" + user.getUsername()
                            + "' didn't completed successfully the authentication so it was sent to the AuthServer");
                    this.serverUtils.bungee().sendToServer(player, this.authServer);
                }
            }
        }));
    }
}
