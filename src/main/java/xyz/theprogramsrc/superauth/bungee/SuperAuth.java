package xyz.theprogramsrc.superauth.bungee;

import java.util.List;

import xyz.theprogramsrc.superauth.bungee.commands.CrackedCommand;
import xyz.theprogramsrc.superauth.bungee.commands.MainCommand;
import xyz.theprogramsrc.superauth.bungee.commands.PremiumCommand;
import xyz.theprogramsrc.superauth.bungee.listeners.PlayerListeners;
import xyz.theprogramsrc.superauth.bungee.listeners.blockers.ServerChangeBlocker;
import xyz.theprogramsrc.superauth.global.SessionStorage;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.global.vpn_blocker.VPNBlocker;
import xyz.theprogramsrc.supercoreapi.bungee.BungeePlugin;
import xyz.theprogramsrc.supercoreapi.global.files.yml.YMLConfig;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBaseSettings;
import xyz.theprogramsrc.supercoreapi.global.storage.MySQLDataBase;
import xyz.theprogramsrc.supercoreapi.global.utils.ServerUtils;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.libs.hikari.HikariConfig;

public class SuperAuth extends BungeePlugin {

    public static SuperAuth bungee;

    private ServerUtils serverUtils;

    private VPNBlocker vpnBlocker;
    private MySQLDataBase mySQLDataBase;
    private UserStorage userStorage;

    private boolean stop;
    private String authServer;
    private List<String> blockedActions;
    private List<String> enabledCommands;
    private String premiumCommand;
    private String premiumPermission;
    private String crackedCommand;
    private String crackedPermission;

    @Override
    public void onPluginLoad() {
        bungee = this;
        this.log("Instance loaded");
        new SessionStorage(this);
        this.log("Memory Storage Loaded");
    }

    @Override
    public void onPluginEnable() {
        this.registerTranslation(LBase.class);
        this.vpnBlocker = new VPNBlocker(this, this.getSettings().getConfig().getBoolean("VPNBlockerEnabled", true));
        this.log("VPNBlocker Loaded");
        this.serverUtils = new ServerUtils();
        this.log("Server Utils loaded");
        this.loadSettings();
        if(this.stop) return;
        this.userStorage = new UserStorage(this, this.getMySQLDataBase());
        new PlayerListeners();
        this.log("Player Listener Loaded");
        new MainCommand();
        this.log("Command '/superauth' registered (Only for console)");
        if(this.blockedActions.contains("SERVER_CHANGE")){
            new ServerChangeBlocker();
        }

        if(this.enabledCommands.contains("PREMIUM")){
            new PremiumCommand();
            this.log("Command '/" + this.getPremiumCommand().toLowerCase() + "' registered");
        }

        if(this.enabledCommands.contains("CRACKED")){
            new CrackedCommand();
            this.log("Command '/" + this.getCrackedCommand().toLowerCase() + "' registered");
        }
    }

    @Override
    public void onPluginDisable() {
        if(this.stop) return;
        bungee = null;
        this.vpnBlocker = null;
        this.mySQLDataBase = null;
        this.userStorage = null;
    }

    public VPNBlocker getVPNBlocker() {
        return vpnBlocker;
    }

    public MySQLDataBase getMySQLDataBase() {
        return mySQLDataBase;
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public void loadSettings(){
        final YMLConfig cfg = this.getSettings().getConfig();
        if(!cfg.contains("AuthServer")) cfg.add("AuthServer", "Auth");
        this.authServer = cfg.getString("AuthServer");
        if(!cfg.contains("BlockedActions")) cfg.add("BlockedActions", Utils.toList("SERVER_CHANGE"));
        this.blockedActions = cfg.getStringList("BlockedActions");
        if(!cfg.contains("Commands")){
            cfg.add("Commands.Premium", "premium");
            cfg.add("Commands.Cracked", "cracked");
        }

        this.premiumCommand = cfg.getString("Commands.Premium");
        this.crackedCommand = cfg.getString("Commands.Cracked");

        if(!cfg.contains("Permissions")){
            cfg.add("Permissions.PremiumCommand", "command.premium");
            cfg.add("Permissions.CrackedCommand", "command.cracked");
        }

        this.premiumPermission = cfg.getString("Permissions.PremiumCommand");
        this.crackedPermission = cfg.getString("Permissions.CrackedCommand");

        if(!cfg.contains("EnabledCommands")) cfg.add("EnabledCommands", Utils.toList("PREMIUM", "CRACKED"));
        this.enabledCommands = cfg.getStringList("EnabledCommands");

        if(!cfg.contains("MySQL.Host") || !cfg.contains("MySQL.Port") || !cfg.contains("MySQL.DataBase") || !cfg.contains("MySQL.UserName") || !cfg.contains("MySQL.Password") || !cfg.contains("MySQL.UseSSL")){
            if(!cfg.contains("MySQL.Host")) cfg.add("MySQL.Host", "sql.example.com");
            if(!cfg.contains("MySQL.Port")) cfg.add("MySQL.Port", "3306");
            if(!cfg.contains("MySQL.DataBase")) cfg.add("MySQL.DataBase", "superauth");
            if(!cfg.contains("MySQL.UserName")) cfg.add("MySQL.UserName", "superauth");
            if(!cfg.contains("MySQL.Password")) cfg.add("MySQL.Password", Utils.randomPassword(16));
            if(!cfg.contains("MySQL.UseSSL")) cfg.add("MySQL.UseSSL", false);
            if(!cfg.contains("MySQL.ConnectionUrl")) cfg.add("MySQL.ConnectionUrl", "jdbc:mysql://{Host}:{Port}/{Database}?useSSL={UseSSL}");
            this.log("&cPlease fill in the MySQL Settings");
            this.log("&9Information: If the path 'MySQL.Password' is empty, the plugin will generate a random password with 16 characters length");
            this.stop = true;
        }else{
            if(cfg.getString("MySQL.Host").equals("sql.example.com")){
                this.log("&cPlease fill in the MySQL Host!");
                this.stop = true;
            }
        }

        if(this.stop) return;
        this.mySQLDataBase = new MySQLDataBase(this) {
            @Override
            public DataBaseSettings getDataBaseSettings() {
                return new DataBaseSettings() {
                    @Override
                    public String host() {
                        return cfg.getString("MySQL.Host");
                    }

                    @Override
                    public String port() {
                        return cfg.getString("MySQL.Port");
                    }

                    @Override
                    public String database() {
                        return cfg.getString("MySQL.DataBase");
                    }

                    @Override
                    public String username() {
                        return cfg.getString("MySQL.UserName");
                    }

                    @Override
                    public String password() {
                        return cfg.getString("MySQL.Password");
                    }

                    @Override
                    public String getURL() {
                        return cfg.getString("MySQL.ConnectionUrl", "jdbc:mysql://{Host}:{Port}/{Database}?useSSL={UseSSL}");
                    }
                };
            }

            @Override
            public void processSettings(HikariConfig config) {
                config.setMaximumPoolSize(25);
                config.setAutoCommit(true);
                config.setConnectionTimeout(10000);
                config.setIdleTimeout(10000);
                config.setMaxLifetime(10000);
                config.setConnectionTestQuery("SELECT 1");
                config.setPoolName("SuperAuth");
            }
        };
    }

    public String getAuthServer() {
        return authServer;
    }

    public ServerUtils getServerUtils() {
        return serverUtils;
    }

    public String getPremiumCommand() {
        return premiumCommand;
    }

    public String getCrackedCommand() {
        return crackedCommand;
    }

    public String getPremiumPermission() {
        return premiumPermission;
    }

    public String getCrackedPermission() {
        return crackedPermission;
    }
}
