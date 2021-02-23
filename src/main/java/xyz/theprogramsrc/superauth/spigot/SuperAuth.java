package xyz.theprogramsrc.superauth.spigot;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.theprogramsrc.superauth.api.SuperAuthAPIEvent;
import xyz.theprogramsrc.superauth.api.SuperAuthAPIHandler;
import xyz.theprogramsrc.superauth.api.actions.SuperAuthAction;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterLoginEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthAfterRegisterEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeLoginEvent;
import xyz.theprogramsrc.superauth.api.auth.SuperAuthBeforeRegisterEvent;
import xyz.theprogramsrc.superauth.global.CommandFilter;
import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.superauth.global.users.User;
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
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.global.Metrics;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBaseSettings;
import xyz.theprogramsrc.supercoreapi.global.storage.MySQLDataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.SQLiteDataBase;
import xyz.theprogramsrc.supercoreapi.global.updater.SongodaUpdateChecker;
import xyz.theprogramsrc.supercoreapi.global.utils.ServerUtils;
import xyz.theprogramsrc.supercoreapi.global.utils.StringUtils;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.global.utils.VersioningUtil;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotPlugin;
import xyz.theprogramsrc.supercoreapi.spigot.packets.Title;
import xyz.theprogramsrc.supercoreapi.spigot.utils.SpigotConsole;
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
    private LinkedHashMap<String, SuperAuthAction> actions;
    private BlockActionsListener blockActionsListener;
    private JoinListener joinListener;
    private boolean papi;

    private int[] afterLoginTitleTimes, afterRegisterTitleTimes;

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
            this.actions = new LinkedHashMap<>();
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
            this.userStorage = new UserStorage(this,this.dataBase);
            this.log("Loaded User Storage");
            this.authSettings = new AuthSettings();
            this.log("Loaded Auth Settings");
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

            this.papi = this.getSuperUtils().isPlugin("PlaceholderAPI");
            this.checkTimings();

            if(this.papi){
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

    public void afterRegister(Player player){
        this.runEvent(new SuperAuthAfterRegisterEvent(this.getAuthSettings(), this.getUserStorage(), player.getName()));
        CaptchaMemory.i.remove(player.getName());
        WasRegisteredMemory.i.remove(player.getName());
        this.after(player, this.getAuthSettings().getAfterRegister(), true);
        int in = this.afterRegisterTitleTimes[0], stay = this.afterRegisterTitleTimes[1], out = this.afterRegisterTitleTimes[2];
        String title = this.getAuthSettings().getAfterRegisterTitle(), subtitle = this.getAuthSettings().getAfterRegisterSubtitle();
        if(this.papi){
            title = PlaceholderAPI.setPlaceholders(player, title);
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);
        }

        String finalTitle = title, finalSubtitle = subtitle;
        getSpigotTasks().runTaskLater(5L,()-> Title.sendTitle(player, in, stay, out, this.getSuperUtils().color(finalTitle), this.getSuperUtils().color(finalSubtitle)));
    }

    public void afterLogin(Player player){
        this.runEvent(new SuperAuthAfterLoginEvent(this.getAuthSettings(), this.getUserStorage(), player.getName()));
        CaptchaMemory.i.remove(player.getName());
        this.after(player, this.getAuthSettings().getAfterLogin(), false);
        int in = this.afterLoginTitleTimes[0], stay = this.afterLoginTitleTimes[1], out = this.afterLoginTitleTimes[2];
        String title = this.getAuthSettings().getAfterLoginTitle(), subtitle = this.getAuthSettings().getAfterLoginSubtitle();
        if(this.papi){
            title = PlaceholderAPI.setPlaceholders(player, title);
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);
        }

        String finalTitle = title, finalSubtitle = subtitle;
        getSpigotTasks().runTaskLater(5L,()-> Title.sendTitle(player, in, stay, out, this.getSuperUtils().color(finalTitle), this.getSuperUtils().color(finalSubtitle)));
        this.getSpigotTasks().runTaskLater(40L, ()-> ForceLoginMemory.i.remove(player.getName()));
    }

    private void after(final Player player, List<String> actions, boolean register){
        User user = this.userStorage.get(player.getName());
        this.runActions(player, actions, false, register);
        user.setAuthorized(true);
        this.userStorage.save(user);
        getSpigotTasks().runTask(player::closeInventory);
    }

    public void beforeRegister(Player player){
        this.runEvent(new SuperAuthBeforeRegisterEvent(this.getAuthSettings(), this.getUserStorage(), player.getName()));
        this.before(player, this.getAuthSettings().getBeforeRegister(), true);
    }

    public void beforeLogin(Player player){
        this.runEvent(new SuperAuthBeforeLoginEvent(this.getAuthSettings(), this.getUserStorage(), player.getName()));
        this.before(player, this.getAuthSettings().getBeforeLogin(), false);
    }

    private void before(final Player player, List<String> actions, boolean register){
        runActions(player, actions, true, register);
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

    public static void registerAction(JavaPlugin plugin, SuperAuthAction action){
        SuperAuth.spigot.actions.put(action.getPrefix(), action);
        SuperAuth.spigot.log("&c" + plugin.getName() + " &7has registered the action &b" + action.getPrefix());
    }

    private void runActions(Player player, List<String> actions, boolean before, boolean register) {
        new Thread(()->{
            for(String a : actions){
                if(a.startsWith("msg:")){
                    String msg = a.replaceFirst("(msg:)+", "");
                    StringUtils s = new StringUtils(msg);
                    s.placeholder("{DisplayName}", player.getDisplayName());
                    s.placeholder("{Player}", player.getName());
                    s.placeholder("{UUID}", player.getUniqueId().toString());
                    s.placeholder("{World}", player.getWorld().getName());
                    s.placeholder("{COORD_X}", player.getLocation().getBlockX()+"");
                    s.placeholder("{COORD_Y}", (player.getLocation().getBlockY()+1)+"");
                    s.placeholder("{COORD_Z}", player.getLocation().getBlockZ()+"");
                    String end = s.get();
                    if(this.papi){
                        end = PlaceholderAPI.setPlaceholders(player, end);
                    }

                    String finalEnd = end;
                    getSpigotTasks().runTask(()->this.getSuperUtils().sendMessage(player, finalEnd));
                }else if(a.startsWith("teleport:")){
                    String stringLocation = a.replaceFirst("(teleport:)+", "");
                    StringUtils s = new StringUtils(stringLocation);
                    s.placeholder("{World}", player.getWorld().getName());
                    s.placeholder("{COORD_X}", player.getLocation().getBlockX()+"");
                    s.placeholder("{COORD_Y}", (player.getLocation().getBlockY()+1)+"");
                    s.placeholder("{COORD_Z}", player.getLocation().getBlockZ()+"");
                    s.placeholder("{DisplayName}", player.getDisplayName());
                    s.placeholder("{Player}", player.getName());
                    s.placeholder("{UUID}", player.getUniqueId().toString());
                    String end = s.get();
                    if(this.papi){
                        end = PlaceholderAPI.setPlaceholders(player, end);
                    }

                    String finalEnd = end;
                    String[] args = finalEnd.split(";");
                    if(args.length < 6) {
                        this.log("&cThe teleport format is: &7World;X;Y;Z;YAW;PITCH&c Example: &7Spawn;0.5;150.0;0.5;0.0;90.0");
                        continue;
                    }
                    World world = Bukkit.getWorld(args[0]);
                    if(world == null) {
                        this.log("&cThe world &7" + args[0] + "&c cannot be found.");
                        continue;
                    }
                    double x = Double.parseDouble(args[1]),
                           y = Double.parseDouble(args[2]),
                           z = Double.parseDouble(args[3]);
                    float yaw = Float.parseFloat(args[4]),
                          pitch = Float.parseFloat(args[5]);
                    getSpigotTasks().runTask(() -> player.teleport(new Location(world, x, y, z, yaw, pitch)));
                }else if(a.startsWith("cmd:")){
                    String command = a.replaceFirst("(cmd:)+", "");
                    StringUtils s = new StringUtils(command);
                    s.placeholder("{DisplayName}", player.getDisplayName());
                    s.placeholder("{Player}", player.getName());
                    s.placeholder("{UUID}", player.getUniqueId().toString());
                    s.placeholder("{World}", player.getWorld().getName());
                    s.placeholder("{COORD_X}", player.getLocation().getBlockX()+"");
                    s.placeholder("{COORD_Y}", (player.getLocation().getBlockY()+1)+"");
                    s.placeholder("{COORD_Z}", player.getLocation().getBlockZ()+"");
                    String end = s.get();
                    if(this.papi){
                        end = PlaceholderAPI.setPlaceholders(player, end);
                    }

                    String finalEnd = end;
                    getSpigotTasks().runTask(()-> SuperAuth.spigot.getServer().dispatchCommand(player, finalEnd));
                }else if(a.startsWith("console:")){
                    SpigotConsole console = new SpigotConsole();
                    String command = a.replaceFirst("(console:)+", "");
                    StringUtils s = new StringUtils(command);
                    s.placeholder("{UUID}", player.getUniqueId().toString());
                    s.placeholder("{DisplayName}", player.getDisplayName());
                    s.placeholder("{Player}", player.getName());
                    s.placeholder("{COORD_X}", player.getLocation().getBlockX()+"");
                    s.placeholder("{COORD_Y}", (player.getLocation().getBlockY()+1)+"");
                    s.placeholder("{COORD_Z}", player.getLocation().getBlockZ()+"");
                    s.placeholder("{World}", player.getWorld().getName());
                    String end = s.get();
                    if(this.papi){
                        end = PlaceholderAPI.setPlaceholders(player, end);
                    }

                    String finalEnd = end;
                    getSpigotTasks().runTask(()->console.execute(finalEnd));
                }else if(a.startsWith("server:")){
                    String server = a.replaceFirst("(server:)+", "");
                    StringUtils s = new StringUtils(server);
                    s.placeholder("{DisplayName}", player.getDisplayName());
                    s.placeholder("{Player}", player.getName());
                    s.placeholder("{UUID}", player.getUniqueId().toString());
                    String end = s.get();
                    if(this.papi){
                        end = PlaceholderAPI.setPlaceholders(player, end);
                    }

                    String finalEnd = end;
                    getSpigotTasks().runTask(()->this.serverUtils.spigot().sendToServer(player, finalEnd));
                }else if(a.startsWith("wait:")){
                    String pauseString = a.replaceFirst("(wait:)+", "");
                    if(!Utils.isInteger(pauseString)){
                        this.log("&cWait Action must be an integer! &7Current Value: &e" + pauseString);
                    }else{
                        try{
                            long time = Integer.parseInt(pauseString)*1000L;
                            Thread.sleep(time);
                        }catch (Exception e){
                            this.addError(e);
                            e.printStackTrace();
                        }
                    }
                }else{
                    for (Map.Entry<String, SuperAuthAction> entry : this.actions.entrySet()) {
                        String prefix = entry.getKey()+":";
                        if(a.startsWith(prefix)){
                            String argument = a.replaceFirst("(" + entry.getKey() + ":)+", "");
                            entry.getValue().onExecute(player, argument, before, register);
                        }
                    }
                }
            }
        }).start();
    }

    private void checkTimings(){
        int in = 10, stay = 20, out = 10;
        String[] times = this.getAuthSettings().getAfterLoginTitleTime();
        if(times.length <= 2){
            this.log("&cThe path &9'Title-Time.After.Login'&c in &9'AuthSettings.yml'&c must have three arguments!");
            this.log("&aDefault: &e10;20;10");
        }else{
            for (String s : times){
                if(!Utils.isInteger(s)){
                    this.log("&cThe value '" + s + "' in 'Title-Time.After.Login' is not an integer!");
                }
            }

            if(Utils.isInteger(times[0])){
                in = Integer.parseInt(times[0]);
            }
            if(Utils.isInteger(times[1])){
                stay = Integer.parseInt(times[1]);
            }
            if(Utils.isInteger(times[2])){
                out = Integer.parseInt(times[2]);
            }
        }
        this.afterLoginTitleTimes = new int[]{in,stay,out};

        times = this.getAuthSettings().getAfterRegisterTitleTime();
        if(times.length <= 2){
            this.log("&cThe path &9'Title-Time.After.Register'&c in &9'AuthSettings.yml'&c must have three arguments!");
            this.log("&aDefault: &e10;20;10");
        }else{
            for (String s : times){
                if(!Utils.isInteger(s)){
                    this.log("&cThe value '" + s + "' in 'Title-Time.After.Register' is not an integer!");
                }
            }

            if(Utils.isInteger(times[0])){
                in = Integer.parseInt(times[0]);
            }
            if(Utils.isInteger(times[1])){
                stay = Integer.parseInt(times[1]);
            }
            if(Utils.isInteger(times[2])){
                out = Integer.parseInt(times[2]);
            }
        }
        this.afterRegisterTitleTimes = new int[]{in,stay,out};
    }
}
