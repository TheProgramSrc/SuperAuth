package xyz.theprogramsrc.superauth.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.theprogramsrc.superauth.api.SuperAuthAPIEvent;
import xyz.theprogramsrc.superauth.api.SuperAuthAPIHandler;
import xyz.theprogramsrc.superauth.global.CommandFilter;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.global.vpn_blocker.VPNBlocker;
import xyz.theprogramsrc.superauth.spigot.commands.AuthCommand;
import xyz.theprogramsrc.superauth.spigot.commands.LoginCommand;
import xyz.theprogramsrc.superauth.spigot.commands.RegisterCommand;
import xyz.theprogramsrc.superauth.spigot.commands.SuperAuthCommand;
import xyz.theprogramsrc.superauth.spigot.hooks.PlaceholderAPIHook;
import xyz.theprogramsrc.superauth.spigot.listeners.*;
import xyz.theprogramsrc.superauth.spigot.memory.CaptchaMemory;
import xyz.theprogramsrc.superauth.spigot.memory.ForceLoginMemory;
import xyz.theprogramsrc.superauth.spigot.memory.WasRegisteredMemory;
import xyz.theprogramsrc.superauth.spigot.objects.AuthAction;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.Metrics;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBaseSettings;
import xyz.theprogramsrc.supercoreapi.global.storage.MySQLDataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.SQLiteDataBase;
import xyz.theprogramsrc.supercoreapi.global.updater.SongodaUpdateChecker;
import xyz.theprogramsrc.supercoreapi.global.utils.ServerUtils;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.global.utils.VersioningUtil;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotPlugin;
import xyz.theprogramsrc.supercoreapi.spigot.utils.storage.SpigotYMLConfig;

import java.util.*;

public class SuperAuth extends SpigotPlugin {

    public static SuperAuth spigot;
    private VPNBlocker vpnBlocker;
    private UserStorage userStorage;
    private DataBase dataBase;
    private AuthSettings authSettings;
    private ServerUtils serverUtils;
    private List<SuperAuthAPIHandler> handlers;
    private BlockActionsListener blockActionsListener;
    private JoinListener joinListener;
    public static LinkedHashMap<UUID, Long> actionThreadIds;
    public SpigotYMLConfig authActionsConfig;

    @Override
    public void onPluginLoad() {
        spigot = this;
        try{
            this.vpnBlocker = new VPNBlocker(this);
            new CaptchaMemory();
            new WasRegisteredMemory();
            new ForceLoginMemory();
            this.log("Loaded Memory Storage");
            this.handlers = new ArrayList<>();
            actionThreadIds = new LinkedHashMap<>();
        }catch (Exception e){
            this.addError(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginEnable() {
        try{
            this.registerTranslation(LBase.class);
            this.setupGithubTranslationDownloader("TheProgramSrc", "PluginsResources", "superauth");
            this.log("Default translation loaded");
            this.serverUtils = new ServerUtils();
            this.log("Server Utils loaded");
            this.setupSettings();
            if(this.isEmergencyStop()) return;
            this.userStorage = new UserStorage(this, this.dataBase);
            this.log("Loaded User Storage");
            this.authSettings = new AuthSettings();
            this.log("Loaded Auth Settings");
            this.authActionsConfig = new SpigotYMLConfig(this.getPluginFolder(), "AuthActions.yml");
            AuthAction.registerDefaults();
            this.log("Loaded Auth Actions");
            LinkedList<String> filteredCommands = new LinkedList<>(Utils.toList(this.authSettings.getAuthCommand(), this.authSettings.getLoginCommand(), this.authSettings.getRegisterCommand()));
            filteredCommands.addAll(this.authSettings.getLoginAliases());
            filteredCommands.addAll(this.authSettings.getRegisterAliases());
            filteredCommands.addAll(this.authSettings.getAuthAliases());
            new CommandFilter(Utils.toStringArray(filteredCommands)).register();
            this.log("Loaded Command Filter");
            new PreLoginListener();
            this.log("Loaded Pre-Login Listener");
            this.joinListener = new JoinListener();
            this.log("Loaded Join Listener");
            new GeneralListeners();
            new SkinSyncListener();
            new IPSyncListener();
            this.log("Loaded General Listeners");
            this.blockActionsListener = new BlockActionsListener();
            if(this.authSettings.isAuthEnabled()){
                this.debug("Registering commands...");
                new AuthCommand();
                this.log("Registered '/" + this.getAuthSettings().getAuthCommand().toLowerCase() + "' command");
                new RegisterCommand();
                this.log("Registered '/"+this.getAuthSettings().getRegisterCommand().toLowerCase()+"' command");
                new LoginCommand();
                this.log("Registered '/"+this.getAuthSettings().getLoginCommand().toLowerCase()+"' command");
            }
            new SuperAuthCommand();
            this.log("Registered '/sauth' command");
            this.log("&aIf you need help first check the wiki:&b https://wiki.theprogramsrc.xyz");
            this.log("&aIf you need direct support join to our discord:&b https://go.theprogramsrc.xyz/discord");
            this.checkBungeeMySQL();
            this.updateChecker();

            boolean papi = this.getSuperUtils().isPlugin("PlaceholderAPI");

            if(papi){
                new PlaceholderAPIHook().register();
                this.log("&aPlaceholderAPI Hook Registered.");
            }

            new Metrics(this, 7004);
        }catch (Exception e){
            this.addError(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onPluginDisable() {
        this.dataBase = null;
        this.userStorage = null;
        this.vpnBlocker = null;
        spigot = null;
    }

    public JoinListener getJoinListener() {
        return this.joinListener;
    }

    public BlockActionsListener getBlockActionsListener() {
        return this.blockActionsListener;
    }

    public VPNBlocker getVPNBlocker() {
        return vpnBlocker;
    }

    public DataBase getDataBase() {
        return dataBase;
    }

    public UserStorage getUserStorage() {
        return this.userStorage;
    }

    public boolean isSQLite(){
        return this.dataBase instanceof SQLiteDataBase;
    }

    public AuthSettings getAuthSettings() {
        return authSettings;
    }

    public ServerUtils getServerUtils() {
        return serverUtils;
    }

    private void checkBungeeMySQL(){
        if(this.isBungeeEnabled() && this.isSQLite()){
            this.log("It seems like you have BungeeCord enabled in your configuration");
            this.log("It's recommended to use MySQL on networks!");
        }
    }

    private void setupSettings(){
        final SpigotYMLConfig cfg = this.getSettingsStorage().getConfig();
        if(!cfg.contains("UpdateChecker")) cfg.add("UpdateChecker", true);
        if(!cfg.contains("MySQL.Enabled") || !cfg.contains("MySQL.Host") || !cfg.contains("MySQL.Port") || !cfg.contains("MySQL.DataBase") || !cfg.contains("MySQL.UserName") || !cfg.contains("MySQL.Password") || !cfg.contains("MySQL.UseSSL")){
            if(!cfg.contains("MySQL.Enabled")) cfg.add("MySQL.Enabled", false);
            if(!cfg.contains("MySQL.Host")) cfg.add("MySQL.Host", "sql.example.com");
            if(!cfg.contains("MySQL.Port")) cfg.add("MySQL.Port", "3306");
            if(!cfg.contains("MySQL.DataBase")) cfg.add("MySQL.DataBase", "superauth");
            if(!cfg.contains("MySQL.UserName")) cfg.add("MySQL.UserName", "superauth");
            if(!cfg.contains("MySQL.Password")) cfg.add("MySQL.Password", Utils.randomPassword(16));
            if(!cfg.contains("MySQL.UseSSL")) cfg.add("MySQL.UseSSL", false);
            if(this.isFirstStart()){
                this.log("&cPlease fill in the MySQL Settings. If you're going to use SQLite just start the server again");
                this.log("&9Information: If the path 'MySQL.Password' doesn't exists, the plugin will generate a random password with 16 characters length");
                this.emergencyStop();
                return;
            }
        }


        if(cfg.getBoolean("MySQL.Enabled")){
            if(cfg.getString("MySQL.Host").equals("sql.example.com")){
                this.log("&cPlease fill in the MySQL Host!");
                this.emergencyStop();
                return;
            }
        }

        if(this.isEmergencyStop()) return;

        if(cfg.getBoolean("MySQL.Enabled")){
            this.dataBase = new MySQLDataBase(this) {
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
                    };
                }
            };
        }else{
            this.dataBase = new SQLiteDataBase(this) {
                @Override
                public DataBaseSettings getDataBaseSettings() {
                    return null;
                }
            };
        }
    }

    private void updateChecker(){
        if(!this.getSettingsStorage().getConfig().contains("UpdateChecker")) this.getSettingsStorage().getConfig().add("UpdateChecker", true);
        if(this.getSettingsStorage().getConfig().getBoolean("UpdateChecker")){
            new SongodaUpdateChecker("superauth-secure-your-users-and-server") {
                @Override
                public void onFailCheck() {
                    SuperAuth.this.log("&cError while checking for updates");
                }

                @Override
                public void onSuccessCheck(String s) {
                    int r = VersioningUtil.checkVersions(SuperAuth.this.getPluginVersion(), s);
                    if(r == 1){
                        SuperAuth.this.log(String.format("&bUpdate Found &7(v%s). Please update the plugin (http://songoda.com/marketplace/product/255)", s));
                    }else if(r == 2){
                        SuperAuth.this.log("&cIt seems like you're running a non-release version. Please be careful and do not use this on production");
                        SuperAuth.this.log("&cIf you find any bug please report it to the dev");
                    }else{
                        SuperAuth.this.log("&aYou're using the latest version!");
                    }
                }
            }.checkUpdates();
        }
    }

    public void runEvent(SuperAuthAPIEvent event){
        this.handlers.forEach(api-> api.onEvent(event));
    }

    public static void registerAPIHandler(JavaPlugin plugin, SuperAuthAPIHandler superAuthAPIHandler) {
        SuperAuth.spigot.handlers.add(superAuthAPIHandler);
        SuperAuth.spigot.log("&c" + plugin.getName() + " &7has registered an API Handler");
    }
}
