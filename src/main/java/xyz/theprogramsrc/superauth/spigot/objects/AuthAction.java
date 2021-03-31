package xyz.theprogramsrc.superauth.spigot.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;
import xyz.theprogramsrc.supercoreapi.spigot.utils.storage.SpigotYMLConfig;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class AuthAction extends SpigotModule {

    private final static LinkedHashMap<String, AuthAction> actions = new LinkedHashMap<>();
    private final SpigotYMLConfig cfg = SuperAuth.spigot.authActionsConfig;
    private final String id;

    public AuthAction(String id){
        this.debug("Trying to register AuthAction with id '" + id + "'");
        this.id = id;
        if(actions.containsKey(id)){
            this.debug("The action already exists!");
            this.log("&cThe action &7" + id + "&c is being registered twice. To avoid bugs the plugin won't rewrite it. Just ignore the registration attempt.");
        }else{
            this.debug("Checking configuration file...");
            if(this.cfg.getBoolean("AuthActions." + id, true)){
                this.debug("Action registered");
                actions.put(id, this);
            }else{
                this.debug("Registration ignored: Action disabled in config.");
            }
        }
    }

    public boolean isActionEnabled(){
        return this.cfg.getBoolean("AuthActions." + this.id, true);
    }

    public void toggleAction(){
        this.cfg.set("AuthActions." + this.id, !this.isActionEnabled());
    }

    public String getId() {
        return this.id;
    }

    public AuthActionFlags[] getFlags(){
        return new AuthActionFlags[]{ AuthActionFlags.ALL };
    }

    public abstract void onExecute(String argument, Player player);

    public boolean canExecute(boolean before, boolean login){
        if(!this.isActionEnabled()) return false;
        List<AuthActionFlags> flags = Utils.toList(this.getFlags());
        if(flags.contains(AuthActionFlags.ALL)) return true;
        if(before){
            if(flags.contains(AuthActionFlags.RUN_BEFORE_AUTH)) return true;
            return flags.contains(login ? AuthActionFlags.RUN_BEFORE_LOGIN : AuthActionFlags.RUN_BEFORE_REGISTER);
        }else{
            if(flags.contains(AuthActionFlags.RUN_AFTER_AUTH))  return true;
            return flags.contains(login ? AuthActionFlags.RUN_AFTER_LOGIN : AuthActionFlags.RUN_AFTER_REGISTER);
        }
    }

    public static AuthAction[] authActions(){
        return actions.values().toArray(new AuthAction[0]);
    }

    public static AuthAction fromId(String id){
        return actions.get(id);
    }

    public static void registerDefaults(){
        // Message
        new AuthAction("msg"){
            @Override
            public void onExecute(String argument, Player player) {
                this.getSuperUtils().sendMessage(player, argument);
            }
        };

        // Player Command
        new AuthAction("cmd"){
            @Override
            public void onExecute(final String argument, final Player player) {
                this.getSpigotTasks().runTask(() -> Bukkit.dispatchCommand(player, argument));
            }
        };

        // Console Command
        new AuthAction("console"){
            @Override
            public void onExecute(final String argument, final Player player) {
                this.getSpigotTasks().runTask(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), argument));
            }
        };
        
        // Teleport
        new AuthAction("teleport"){
            @Override
            public void onExecute(String argument, Player player) {
                String[] args = argument.split(";");
                if(args.length < 6) {
                    this.log("&cThe teleport format is: &7World;X;Y;Z;YAW;PITCH&c Example: &7Spawn;0.5;150.0;0.5;0.0;90.0");
                }else{
                    World world = Bukkit.getWorld(args[0]);
                    if(world == null) {
                        this.log("&cThe world &7" + args[0] + "&c cannot be found.");
                    }else{
                        double x = Double.parseDouble(args[1]), y = Double.parseDouble(args[2]), z = Double.parseDouble(args[3]);
                        float yaw = Float.parseFloat(args[4]), pitch = Float.parseFloat(args[5]);
                        getSpigotTasks().runTask(() -> player.teleport(new Location(world, x, y, z, yaw, pitch)));
                    }
                }
            }
        };
        
        // Server Send
        new AuthAction("server"){
            @Override
            public void onExecute(String argument, Player player) {
                getSpigotTasks().runTask(()-> SuperAuth.spigot.getServerUtils().spigot().sendToServer(player, argument));
            }
        };

        // Wait
        new AuthAction("wait"){
            @Override
            public void onExecute(String argument, Player player) {
                if(!Utils.isInteger(argument)){
                    this.log("&cThe argument &7" + argument + "&c is not an integer for the action &7wait&c!");
                }else{
                    int wait = Integer.parseInt(argument);
                    try{
                        Thread.sleep(wait);
                    }catch (InterruptedException ignored) {}
                }
            }
        };
        
        // Last Location
        new AuthAction("lastlocation"){

            private SpigotYMLConfig cfg;

            @Override
            public void onLoad() {
                super.onLoad();
                this.cfg = new SpigotYMLConfig(this.getPluginFolder(), "LastLocation.yml");
            }

            @Override
            public void onExecute(String argument, Player player) {
                String path = "Locations." + player.getUniqueId();
                if(argument.equalsIgnoreCase("register")){
                    String data = this.locToString(player.getLocation());
                    this.cfg.set(path, data);
                }else if(argument.equalsIgnoreCase("back")){
                    if(!this.cfg.contains(path)){
                        throw new RuntimeException("Failed to retrieve previous location of player. Please register the location first.");
                    }else{
                        Location location = this.locFromString(this.cfg.getString(path));
                        if(location != null){
                            this.getSpigotTasks().runTask(() -> player.teleport(location));
                        }
                    }
                }else{
                    throw new IllegalArgumentException("Unrecognized argument '" + argument + "'. Available arguments: register, back");
                }
            }

            private Location locFromString(String data){
                String[] args = data.split(";");
                if(args.length <= 5){
                    return null;
                }else{
                    World world = Bukkit.getWorld(args[0]);
                    if(world == null){
                        throw new NullPointerException("The world '" + args[0] + "' couldn't be found");
                    }else{
                        double x = Double.parseDouble(args[1]),
                                y = Double.parseDouble(args[2]),
                                z = Double.parseDouble(args[3]);
                        float yaw = Float.parseFloat(args[4]),
                                pitch = Float.parseFloat(args[5]);
                        return new Location(world, x, y, z, yaw, pitch);
                    }
                }
            }

            private String locToString(Location loc){
                if(loc.getWorld() == null) return null;
                return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
            }
        };
    }

    public void run(String argument, Player player) {
        long id = Thread.currentThread().getId();
        long saved = SuperAuth.actionThreadIds.containsKey(player.getUniqueId()) ? SuperAuth.actionThreadIds.get(player.getUniqueId()) : -1;
        if(saved != id || !SuperAuth.actionThreadIds.containsKey(player.getUniqueId())){
            this.log("&cThe action &7" + this.id + "&c is running in a non-action thread! To avoid problems the plugin will ignore this action");
        }else{
            this.onExecute(argument, player);
        }
    }

    enum AuthActionFlags {
        RUN_BEFORE_LOGIN,
        RUN_AFTER_LOGIN,
        RUN_BEFORE_REGISTER,
        RUN_AFTER_REGISTER,
        RUN_BEFORE_AUTH,
        RUN_AFTER_AUTH,
        ALL
    }
}