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
    private int[] afterLoginTitleTimes, afterRegisterTitleTimes;

    public AuthSettings(){
        super(new File(SuperAuth.spigot.getDataFolder(), "AuthSettings.yml"));
        this.load();
        this.checkTimings();
    }

    public void load(){
        if(!this.contains("AuthEnabled")) this.add("AuthEnabled", true);
        if(!this.contains("HashingMethod")) this.add("HashingMethod", "SHA512");
        if(!this.contains("AuthMethod")) this.add("AuthMethod", "DIALOG");
        if(!this.contains("PinLength")) this.add("PinLength", 4);
        if(!this.contains("PasswordLength.Min")) this.add("PasswordLength.Min", 8);
        if(!this.contains("PasswordLength.Max")) this.add("PasswordLength.Max", 12);
        if(!this.contains("Commands.Register")) this.add("Commands.Register", "register");
        if(!this.contains("Commands.Login")) this.add("Commands.Login", "login");
        if(!this.contains("Commands.Auth")) this.add("Commands.Auth", "auth");
        if(!this.contains("Commands.Aliases.Register")) this.add("Commands.Aliases.Register", Utils.toList("reg", "r"));
        if(!this.contains("Commands.Aliases.Login")) this.add("Commands.Aliases.Login", Utils.toList("l"));
        if(!this.contains("Commands.Aliases.Auth")) this.add("Commands.Aliases.Auth", Utils.toList("pin"));
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
        if(!this.contains("BlockedActions")) this.add("BlockedActions", Utils.toList("BLOCK_BREAK", "BLOCK_PLACE", "CHAT", "MOVEMENT", "INTERACTION", "CUSTOM_INVENTORY", "DAMAGE"));
        if(!this.contains("Auth.CommandUsageTimer")) this.add("Auth.CommandUsageTimer", 3);
        if(!this.contains("Auth.PremiumAutoLogin")) this.add("Auth.PremiumAutoLogin", true);
    }

    @Override
    public void reload() {
        super.reload();
        this.authMethod = AuthMethod.of(this.getString("AuthMethod"));
    }

    public boolean getPremiumAutoLogin(){
        return this.getBoolean("Auth.PremiumAutoLogin", true);
    }

    public int getCommandUsageTimer(){
        return this.getInt("Auth.CommandUsageTimer", 3);
    }

    public AuthMethod getAuthMethod() {
        return authMethod == null ? AuthMethod.of(this.getString("AuthMethod")) : this.authMethod;
    }

    public List<String> getLoginAliases(){
        return this.getStringList("Commands.Aliases.Login", Utils.toList());
    }

    public List<String> getRegisterAliases(){
        return this.getStringList("Commands.Aliases.Register", Utils.toList());
    }

    public List<String> getAuthAliases(){
        return this.getStringList("Commands.Aliases.Auth", Utils.toList());
    }

    public List<String> getBeforeRegister(){
        return this.getStringList("Before.Register", Utils.toList());
    }

    public List<String> getBeforeLogin(){
        return this.getStringList("Before.Login", Utils.toList());
    }

    public List<String> getAfterRegister(){
        return this.getStringList("After.Register", Utils.toList());
    }

    public List<String> getAfterLogin(){
        return this.getStringList("After.Login", Utils.toList());
    }

    public int getMaxTime(){
        return this.getInt("AntiBots.MaxTime", 30);
    }

    public boolean isCaptchaEnabled(){
        return this.getBoolean("AntiBots.Captcha.Enabled", true);
    }

    public double getCaptchaChance(){
        return this.getDouble("AntiBots.Captcha.Chance", 0.9D);
    }

    public int getCaptchaLength(){
        return this.getInt("AntiBots.Captcha.Length", 5);
    }

    public HashingMethod getHashingMethod(){
        return HashingMethod.getOrDefault(this.getString("HashingMethod", "SHA512"), HashingMethod.SHA512);
    }

    public String getLoginCommand(){
        return this.getString("Commands.Login", "login").toLowerCase();
    }

    public String getRegisterCommand(){
        return this.getString("Commands.Register", "register").toLowerCase();
    }

    public String getAuthCommand(){
        return this.getString("Commands.Auth", "auth").toLowerCase();
    }

    public List<String> getWhitelistedCommands(){
        return this.getStringList("WhitelistedCommands");
    }

    public List<String> getBlockedActions(){
        return this.getStringList("BlockedActions");
    }

    public int getPinLength() {
        return this.getInt("PinLength", 4);
    }

    public boolean isAuthEnabled(){
        return this.getBoolean("AuthEnabled", true);
    }

    public String getAfterLoginTitle(){
        return this.getString("Title.After.Login", "&bLogged In!");
    }

    public String getAfterRegisterTitle(){
        return this.getString("Title.After.Register", "&bRegistered In!");
    }

    public String getAfterLoginSubtitle(){
        return this.getString("Subtitle.After.Login", "&7Now you can play!");
    }

    public String getAfterRegisterSubtitle(){
        return this.getString("Subtitle.After.Register", "&7Thank you for choosing us!");
    }

    public String[] getAfterLoginTitleTime(){
        if(!this.contains("Title-Time.After.Login")){
            return new String[]{"10","20","10"};
        }
        return this.getString("Title-Time.After.Login").split(";");
    }
    public String[] getAfterRegisterTitleTime(){
        if(!this.contains("Title-Time.After.Register")){
            return new String[]{"10","20","10"};
        }
        return this.getString("Title-Time.After.Register").split(";");
    }

    public int getMinPasswordLength(){
        return this.getInt("PasswordLength.Min", 8);
    }

    public int getMaxPasswordLength(){
        return this.getInt("PasswordLength.Max", 12);
    }

    public int[] getAfterLoginTitleTimes() {
        return afterLoginTitleTimes;
    }

    public int[] getAfterRegisterTitleTimes() {
        return afterRegisterTitleTimes;
    }

    private void checkTimings(){
        int in = 10, stay = 20, out = 10;
        String[] times = this.getAfterLoginTitleTime();
        if(times.length <= 2){
            SuperAuth.spigot.log("&cThe path &9'Title-Time.After.Login'&c in &9'AuthSettings.yml'&c must have three arguments!");
            SuperAuth.spigot.log("&aDefault: &e10;20;10");
        }else{
            for (String s : times){
                if(!Utils.isInteger(s)){
                    SuperAuth.spigot.log("&cThe value '" + s + "' in 'Title-Time.After.Login' is not an integer!");
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

        times = this.getAfterRegisterTitleTime();
        if(times.length <= 2){
            SuperAuth.spigot.log("&cThe path &9'Title-Time.After.Register'&c in &9'AuthSettings.yml'&c must have three arguments!");
            SuperAuth.spigot.log("&aDefault: &e10;20;10");
        }else{
            for (String s : times){
                if(!Utils.isInteger(s)){
                    SuperAuth.spigot.log("&cThe value '" + s + "' in 'Title-Time.After.Register' is not an integer!");
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
