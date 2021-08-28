package xyz.theprogramsrc.superauth.spigot.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.objects.AuthMethod;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;
import xyz.theprogramsrc.supercoreapi.spigot.SpigotModule;

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

                if(blocked.contains("DAMAGE")){
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event){
        Player player = event.getPlayer();
        this.userStorage.get(player.getName(), user -> {
            if(user == null) return;
            if(!user.isAuthorized()){
                if(user.getAuthMethod() == null || !user.isRegistered()){
                    if(this.settings.getAuthMethod() == AuthMethod.DIALOG){
                        event.setCancelled(true);
                    }else if(this.settings.getAuthMethod() == AuthMethod.COMMANDS){
                        List<String> whitelist = this.settings.getWhitelistedCommands();
                        whitelist.add(this.settings.getRegisterCommand());
                        whitelist.add(this.settings.getLoginCommand());
                        whitelist.addAll(this.settings.getLoginAliases());
                        whitelist.addAll(this.settings.getRegisterAliases());
                        if (whitelist.stream().noneMatch(s-> event.getMessage().contains(s))) {
                            event.setCancelled(true);
                        }
                    }else if(this.settings.getAuthMethod() == AuthMethod.GUI){
                        List<String> whitelist = this.settings.getWhitelistedCommands();
                        whitelist.add(this.settings.getAuthCommand());
                        whitelist.addAll(this.settings.getAuthAliases());
                        if (whitelist.stream().noneMatch(s-> event.getMessage().contains(s))) {
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
                        whitelist.addAll(this.settings.getLoginAliases());
                        whitelist.addAll(this.settings.getRegisterAliases());
                        if (whitelist.stream().noneMatch(s-> event.getMessage().contains(s))) {
                            event.setCancelled(true);
                        }
                    }else if(user.getAuthMethod().equalsIgnoreCase("GUI")){
                        List<String> whitelist = this.settings.getWhitelistedCommands();
                        whitelist.add(this.settings.getAuthCommand());
                        whitelist.addAll(this.settings.getAuthAliases());
                        if (whitelist.stream().noneMatch(s-> event.getMessage().contains(s))) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event){
        if(this.settings.getBlockedActions().contains("INTERACTION")){
            Player player = event.getPlayer();
            this.userStorage.isUserAuthorized(player.getName(), false, authorized -> {
                if(!authorized){
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event){
        if(this.settings.getBlockedActions().contains("INTERACTION")){
            Player player = event.getPlayer();
            this.userStorage.isUserAuthorized(player.getName(), false, authorized -> {
                if(!authorized){
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event){
        if(this.settings.getBlockedActions().contains("BLOCK_BREAK")){
            Player player = event.getPlayer();
            this.userStorage.isUserAuthorized(player.getName(), false, authorized -> {
                if(!authorized){
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){
        if(this.settings.getBlockedActions().contains("BLOCK_PLACE")){
            Player player = event.getPlayer();
            this.userStorage.isUserAuthorized(player.getName(), false, authorized -> {
                if(!authorized){
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent event){
        if(this.settings.getBlockedActions().contains("CHAT")){
            final Player player = event.getPlayer();
            this.userStorage.get(player.getName(), user -> {
                if(user != null){
                    final String msg = event.getMessage();
                    if(!user.isAuthorized()){
                        if(user.getAuthMethod() == null){
                            if(this.settings.getAuthMethod() != AuthMethod.DIALOG){
                                List<String> whitelist = this.settings.getWhitelistedCommands();
                                if(this.settings.getAuthMethod() == AuthMethod.COMMANDS){
                                    whitelist.add(this.settings.getRegisterCommand());
                                    whitelist.add(this.settings.getLoginCommand());
                                    whitelist.addAll(this.settings.getLoginAliases());
                                    whitelist.addAll(this.settings.getRegisterAliases());
                                    boolean res = whitelist.stream().noneMatch(s-> msg.startsWith("/" + s));
                                    this.log("Testing commands (" + String.join(", ", whitelist) + ") with result: " + (res ? 'y' : 'n'));
                                    if (res) {
                                        event.setCancelled(true);
                                    }
                                }else{
                                    whitelist.add(this.settings.getAuthCommand());
                                    whitelist.addAll(this.settings.getAuthAliases());
                                    if (whitelist.stream().noneMatch(s-> msg.startsWith("/" + s))) {
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
                                List<String> whitelist = this.settings.getWhitelistedCommands();
                                if(user.getAuthMethod().equals("COMMANDS")){
                                    whitelist.add(this.settings.getRegisterCommand());
                                    whitelist.add(this.settings.getLoginCommand());
                                    whitelist.addAll(this.settings.getLoginAliases());
                                    whitelist.addAll(this.settings.getRegisterAliases());
                                    if (whitelist.stream().noneMatch(s-> msg.startsWith("/" + s))) {
                                        event.setCancelled(true);
                                    }
                                }else{
                                    whitelist.add(this.settings.getAuthCommand());
                                    whitelist.addAll(this.settings.getAuthAliases());
                                    if (whitelist.stream().noneMatch(s-> msg.startsWith("/" + s))) {
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
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event){
        if(this.settings.getBlockedActions().contains("MOVEMENT")){
            Player player = event.getPlayer();
            this.userStorage.isUserAuthorized(player.getName(), false, authorized -> {
                if(!authorized){
                    event.setCancelled(true);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage2(EntityDamageByEntityEvent e){
        this.onDamage(e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage3(EntityDamageByBlockEvent e){
        this.onDamage(e);
    }

    private void onDamage(EntityDamageEvent e){
        if (e.getEntity() instanceof Player && this.settings.getBlockedActions().contains("DAMAGE")) {
            Player player = (Player) e.getEntity();
            this.userStorage.isUserAuthorized(player.getName(), false, authorized -> {
                if(!authorized){
                    e.setDamage(0);
                    e.setCancelled(true);
                }
            });
        }
    }
}
