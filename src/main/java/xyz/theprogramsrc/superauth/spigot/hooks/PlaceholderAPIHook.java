package xyz.theprogramsrc.superauth.spigot.hooks;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

import java.util.List;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final SuperAuth plugin = SuperAuth.spigot;

    @Override
    public @NotNull String getIdentifier() {
        return "superauth";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TheProgramSrc";
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getPluginVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return Utils.toList("is_premium", "is_registered", "is_authorized", "is_admin");
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) return null;
        User user = this.plugin.getUserStorage().get(player.getName());
        if(user == null) return null;
        if(params.equalsIgnoreCase("is_premium")){
            return this.bool(user.isPremium());
        }else if(params.equalsIgnoreCase("is_registered")){
            return this.bool(user.isRegistered());
        }else if(params.equalsIgnoreCase("is_authorized")){
            return this.bool(user.isAuthorized());
        }else if(params.equalsIgnoreCase("is_admin")){
            return this.bool(user.isAdmin());
        }else if(params.equalsIgnoreCase("auth_method")){
            return user.getAuthMethod();
        }else{
            return null;
        }
    }

    private String bool(boolean bool){
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }
}
