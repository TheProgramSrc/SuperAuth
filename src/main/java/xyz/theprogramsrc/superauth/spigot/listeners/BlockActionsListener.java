package xyz.theprogramsrc.superauth.spigot.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.objects.AuthMethod;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

import java.util.List;

public class BlockActionsListener extends SpigotModule {

    private AuthSettings settings;
    private UserStorage userStorage;

    public BlockActionsListener(){
        super(false);
    }

    @Override
    public void onLoad() {
        HandlerList.unregisterAll(this);
        this.settings = SuperAuth.spigot.getAuthSettings();
        this.userStorage = SuperAuth.spigot.getUserStorage();
        if(this.settings.isAuthEnabled()){
            this.listener(this);
            List<String> blocked = settings.getBlockedActions();
            int enabled = 0;
            try{
                if(blocked.contains("BLOCK_PLACE")){
                    enabled++;
                }

                if(blocked.contains("BLOCK_BREAK")){
                    enabled++;
                }

                if(blocked.contains("CHAT")){
                    enabled++;
                }

                if(blocked.contains("MOVEMENT")){
                    enabled++;
                }

                if(blocked.contains("INTERACTION")){
                    enabled++;
                }
                this.log("Loaded &a" + enabled + "&r Action Blockers");
            }catch (Exception ex){
                this.plugin.addError(ex);
                this.log("&cError while loading action blockers:");
                ex.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        User user = this.userStorage.get(player.getName());
        if(user == null) return;
        if(!user.isAuthorized()){
            if(user.getAuthMethod() == null || !user.isRegistered()){
                if(this.settings.getAuthMethod() == AuthMethod.DIALOG){
                    event.setCancelled(true);
                }else if(this.settings.getAuthMethod() == AuthMethod.COMMANDS){
                    List<String> whitelist = this.settings.getWhitelistedCommands();
                    whitelist.add(this.settings.getRegisterCommand());
                    whitelist.add(this.settings.getLoginCommand());
                    if (whitelist.stream().noneMatch(s-> event.getMessage().contains(s))) {
                        event.setCancelled(true);
                    }
                }else if(this.settings.getAuthMethod() == AuthMethod.GUI){
                    String authCommand = this.settings.getAuthCommand();
                    if(!event.getMessage().contains(authCommand)){
                        event.setCancelled(true);
                    }
                }
            }else{
                if(user.getAuthMethod().equalsIgnoreCase("DIALOG")){
                    event.setCancelled(true);
                }else if(user.getAuthMethod().equalsIgnoreCase("COMMANDS")){
                    List<String> whitelist = this.settings.getWhitelistedCommands();
                    whitelist.add(this.settings.getRegisterCommand());
                    whitelist.add(this.settings.getLoginCommand());
                    if (whitelist.stream().noneMatch(s-> event.getMessage().contains(s))) {
                        event.setCancelled(true);
                    }
                }else if(user.getAuthMethod().equalsIgnoreCase("GUI")){
                    String authCommand = this.settings.getAuthCommand();
                    if (!event.getMessage().contains(authCommand)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if(this.settings.getBlockedActions().contains("INTERACTION")){
            Player player = event.getPlayer();
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(!user.isAuthorized()){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteract1(PlayerInteractAtEntityEvent event){
        if(this.settings.getBlockedActions().contains("INTERACTION")){
            Player player = event.getPlayer();
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(!user.isAuthorized()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteract2(PlayerInteractEntityEvent event){
        if(this.settings.getBlockedActions().contains("INTERACTION")){
            Player player = event.getPlayer();
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(!user.isAuthorized()){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        if(this.settings.getBlockedActions().contains("BLOCK_BREAK")){
            Player player = event.getPlayer();
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(!user.isAuthorized()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        if(this.settings.getBlockedActions().contains("BLOCK_PLACE")){
            Player player = event.getPlayer();
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(!user.isAuthorized()){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(final AsyncPlayerChatEvent event){
        if(this.settings.getBlockedActions().contains("CHAT")){
            final Player player = event.getPlayer();
            final User user = this.userStorage.get(player.getName());
            if(user != null){
                final String msg = event.getMessage();
                this.getSpigotTasks().runTask(()-> {
                    if(!user.isAuthorized()){
                        if(user.getAuthMethod() == null){
                            if(this.settings.getAuthMethod() != AuthMethod.DIALOG){
                                if(this.settings.getAuthMethod() == AuthMethod.COMMANDS){
                                    if(!msg.startsWith("/login") && !msg.startsWith("/register")){
                                        event.setCancelled(true);
                                    }
                                }else{
                                    if(!msg.startsWith("/auth")){
                                        event.setCancelled(true);
                                    }
                                }
                            }else{
                                if(msg.startsWith("/")){
                                    event.setCancelled(true);
                                }
                            }
                        }else{
                            if(!user.getAuthMethod().equals("DIALOG")){
                                if(user.getAuthMethod().equals("COMMANDS")){
                                    if(!msg.startsWith("/login") && !msg.startsWith("/register")){
                                        event.setCancelled(true);
                                    }
                                }else{
                                    if(!msg.startsWith("/auth")){
                                        event.setCancelled(true);
                                    }
                                }
                            }else{
                                if(msg.startsWith("/")){
                                    event.setCancelled(true);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMove(PlayerMoveEvent event){
        if(this.settings.getBlockedActions().contains("MOVEMENT")){
            Player player = event.getPlayer();
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(!user.isAuthorized()){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e){
        if(this.settings.getBlockedActions().contains("CUSTOM_INVENTORY")){
            Player player = ((Player)e.getPlayer());
            User user = this.userStorage.get(player.getName());
            if(user != null){
                if(user.getAuthMethod() == null || !user.isRegistered()){
                    if(this.settings.getAuthMethod() != AuthMethod.GUI){
                        player.closeInventory();
                    }
                }else{
                    if(!user.getAuthMethod().equalsIgnoreCase("GUI")){
                        player.closeInventory();
                    }
                }
            }
        }
    }
}
