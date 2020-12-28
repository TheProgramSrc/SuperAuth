package xyz.theprogramsrc.superauth.spigot.storage;

import xyz.theprogramsrc.superauth.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.superauth.spigot.objects.AuthMethod;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;
import xyz.theprogramsrc.supercoreapi.spigot.utils.storage.SpigotYMLConfig;

import java.io.File;
import java.util.List;

public class AuthSettings extends SpigotYMLConfig {

    private AuthMethod authMethod;

    public AuthSettings(){
        super(new File(SuperAuth.spigot.getDataFolder(), "AuthSettings.yml"));
        this.load();
    }

    public void load(){
        if(!this.contains("AuthEnabled")) this.add("AuthEnabled", true);
        if(!this.contains("HashingMethod")) this.add("HashingMethod", "SHA512");
        if(!this.contains("AuthMethod")) this.add("AuthMethod", "DIALOG");
        if(!this.contains("PinLength")) this.add("PinLength", 3);
        if(!this.contains("PasswordLength.Min")) this.add("PasswordLength.Min", 8);
        if(!this.contains("PasswordLength.Max")) this.add("PasswordLength.Max", 12);
        if(!this.contains("Commands.Register")) this.add("Commands.Register", "register");
        if(!this.contains("Commands.Login")) this.add("Commands.Login", "login");
        if(!this.contains("Commands.Auth")) this.add("Commands.Auth", "auth");
        this.authMethod = AuthMethod.of(this.getString("AuthMethod"));
        if(!this.contains("AntiBots.MaxTime")) this.add("AntiBots.MaxTime", 30);
        if(!this.contains("AntiBots.Captcha.Enabled")) this.add("AntiBots.Captcha.Enabled", true);
        if(!this.contains("AntiBots.Captcha.Chance")) this.add("AntiBots.Captcha.Chance", 0.9D);
        if(!this.contains("AntiBots.Captcha.Length")) this.add("AntiBots.Captcha.Length", 5);
        if(!this.contains("Before.Register")) this.add("Before.Register", Utils.toList("msg:&aHello! Please register yourself"));
        if(!this.contains("Before.Login")) this.add("Before.Login", Utils.toList("msg:&aHello Again! Please authenticate yourself"));
        if(!this.contains("After.Register")) this.add("After.Register", Utils.toList("msg:&aGreat! Now you can play", "cmd:kit nooby", "server:rules"));
        if(!this.contains("After.Login")) this.add("After.Login", Utils.toList("msg:&aGreat! Now you can play", "cmd:kit nooby", "server:lobby"));
        if(!this.contains("WhitelistedCommands")) this.add("WhitelistedCommands", Utils.toList("register", "login"));
        if(!this.contains("Title.After.Login")) this.add("Title.After.Login", "&bLogged In!");
        if(!this.contains("Subtitle.After.Login")) this.add("Subtitle.After.Login", "&7Now you can play!");
        if(!this.contains("Subtitle.After.Register")) this.add("Subtitle.After.Register", "&7Thank you for choosing us!");
        if(!this.contains("Title.After.Register")) this.add("Title.After.Register", "&bRegistered In!");
        if(!this.contains("Title-Time.After.Login")) this.add("Title-Time.After.Login", "10;20;10");
        if(!this.contains("Title-Time.After.Register")) this.add("Title-Time.After.Register", "10;20;10");
        if(!this.contains("BlockedActions")) this.add("BlockedActions", Utils.toList("BLOCK_BREAK", "BLOCK_PLACE", "CHAT", "MOVEMENT", "INTERACTION", "CUSTOM_INVENTORY"));
        if(!this.contains("Auth.CommandUsageTimer")) this.add("Auth.CommandUsageTimer", 3);
    }

    @Override
    public void reload() {
        super.reload();
        this.authMethod = AuthMethod.of(this.getString("AuthMethod"));
    }

    public int getCommandUsageTimer(){
        return this.getInt("Auth.CommandUsageTimer");
    }

    public AuthMethod getAuthMethod() {
        return authMethod == null ? AuthMethod.of(this.getString("AuthMethod")) : this.authMethod;
    }

    public List<String> getBeforeRegister(){
        return this.getStringList("Before.Register");
    }

    public List<String> getBeforeLogin(){
        return this.getStringList("Before.Login");
    }

    public List<String> getAfterRegister(){
        return this.getStringList("After.Register");
    }

    public List<String> getAfterLogin(){
        return this.getStringList("After.Login");
    }

    public int getMaxTime(){
        return this.contains("AntiBots.MaxTime") ? this.getInt("AntiBots.MaxTime") : 30;
    }

    public boolean isCaptchaEnabled(){
        return !this.contains("AntiBots.Captcha.Enabled") || this.getBoolean("AntiBots.Captcha.Enabled");
    }

    public double getCaptchaChance(){
        return this.contains("AntiBots.Captcha.Chance") ? this.getDouble("AntiBots.Captcha.Chance") : 0.9D;
    }

    public int getCaptchaLength(){
        return this.contains("AntiBots.Captcha.Length") ? this.getInt("AntiBots.Captcha.Length") : 5;
    }

    public HashingMethod getHashingMethod(){
        return HashingMethod.getOrDefault(this.getString("HashingMethod"), HashingMethod.SHA512);
    }

    public String getLoginCommand(){
        return this.getString("Commands.Login").toLowerCase();
    }

    public String getRegisterCommand(){
        return this.getString("Commands.Register").toLowerCase();
    }

    public String getAuthCommand(){
        return this.getString("Commands.Auth").toLowerCase();
    }

    public List<String> getWhitelistedCommands(){
        return this.getStringList("WhitelistedCommands");
    }

    public List<String> getBlockedActions(){
        return this.getStringList("BlockedActions");
    }

    public int getPinLength() {
        return this.getInt("PinLength");
    }

    public boolean isAuthEnabled(){
        return this.getBoolean("AuthEnabled");
    }

    public String getAfterLoginTitle(){
        return this.getString("Title.After.Login");
    }

    public String getAfterRegisterTitle(){
        return this.getString("Title.After.Register");
    }

    public String getAfterLoginSubtitle(){
        return this.getString("Subtitle.After.Login");
    }

    public String getAfterRegisterSubtitle(){
        return this.getString("Subtitle.After.Register");
    }

    public String[] getAfterLoginTitleTime(){
        if(!this.contains("Title-Time.After.Login")){
            return new String[0];
        }
        return this.getString("Title-Time.After.Login").split(";");
    }
    public String[] getAfterRegisterTitleTime(){
        if(!this.contains("Title-Time.After.Register")){
            return new String[0];
        }
        return this.getString("Title-Time.After.Register").split(";");
    }

    public int getMinPasswordLength(){
        return this.getInt("PasswordLength.Min");
    }

    public int getMaxPasswordLength(){
        return this.getInt("PasswordLength.Max");
    }
}
