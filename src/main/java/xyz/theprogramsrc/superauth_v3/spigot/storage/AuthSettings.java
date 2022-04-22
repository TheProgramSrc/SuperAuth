package xyz.theprogramsrc.superauth_v3.spigot.storage;

import java.io.File;
import java.util.List;

import xyz.theprogramsrc.superauth_v3.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.superauth_v3.spigot.objects.AuthMethod;
import xyz.theprogramsrc.supercoreapi.global.files.yml.YMLConfig;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

public class AuthSettings extends YMLConfig {

    private AuthMethod authMethod;
    private int[] afterLoginTitleTimes, afterRegisterTitleTimes;

    public AuthSettings(){
        super(new File(SuperAuth.spigot.getDataFolder(), "AuthSettings.yml"));
        this.add("AuthEnabled", true);
        this.addComment("AuthEnabled", "Should the authentication be enabled?");
        this.add("HashingMethod", "SHA512");
        this.addComment("HashingMethod", "We recommend 'SHA512' for you ;D");
        this.add("AuthMethod", "DIALOG");
        this.addComment("AuthMethod", "We have DIALOG, GUI and COMMANDS");
        this.add("PinLength", 4);
        this.addComment("PinLength","For the GUI Pin, how long should be the pins? (We recommend between 4 and 6)");
        this.add("PasswordLength.Min", 8);
        this.addComment("PasswordLength.Min", "Min password lenght. We recommend you 8");
        this.add("PasswordLength.Max", 12);
        this.addComment("PasswordLength.Max", "Max password lenght. We recommend you from 12 to 16\n(If you set a higher number make sure to add longer timings so users have the time to write their passwords)");
        this.add("Commands.Register", "register");
        this.addComment("Commands.Register","The /register command (Yes, you can change it!)");
        this.add("Commands.Login", "login");
        this.addComment("Commands.Login","The /login command (Yes, you can change it!)");
        this.add("Commands.Auth", "auth");
        this.addComment("Commands.Auth","The /auth command (Yes, you can change it!) <- Only needed if the user can't see the Pin Gui");
        this.add("Commands.Aliases.Register", Utils.toList("reg", "r"));
        this.addComment("Commands.Aliases.Register","The /register command aliases (Yes, you can change it!)");
        this.add("Commands.Aliases.Login", Utils.toList("l"));
        this.addComment("Commands.Aliases.Login","The /login command aliases (Yes, you can change it!)");
        this.add("Commands.Aliases.Auth", Utils.toList("pin"));
        this.addComment("Commands.Aliases.Auth","The /auth command aliases (Yes, you can change it!) <- Only needed if the user can't see the Pin Gui");
        this.authMethod = AuthMethod.of(this.getString("AuthMethod"));
        this.add("AntiBots.MaxTime", 30);
        this.addComment("AntiBots.MaxTime", "The max time (in seconds) that the user can be in authentication.");
        this.add("AntiBots.Captcha.Enabled", true);
        this.addComment("AntiBots.Captcha.Enabled", "Should the captcha be enabled?");
        this.add("AntiBots.Captcha.Chance", 0.9D);
        this.addComment("AntiBots.Captcha.Chance", "The chance that the captcha will be shown (0.0 - 1.0)");
        this.add("AntiBots.Captcha.Length", 5);
        this.addComment("AntiBots.Captcha.Length", "The length of the captcha (We recommend a number between 1 and 10)");
        this.add("AntiBots.BlockIPChanges", true);
        this.addComment("AntiBots.BlockIPChanges", "Should the IP be blocked if the user changes it?");
        this.add("AntiBots.VPNBlocker", true);
        this.addComment("AntiBots.VPNBlocker", "Should the VPN be blocked?");
        this.add("Before.Register", Utils.toList("msg:&aHello! Please register yourself"));
        this.addComment("Before.Register", "The actions that will be executed before the user registers");
        this.add("Before.Login", Utils.toList("msg:&aHello Again! Please authenticate yourself"));
        this.addComment("Before.Login", "The actions that will be executed before the user logs in");
        this.add("After.Register", Utils.toList("msg:&aGreat! Now you can play", "cmd:kit nooby", "server:rules"));
        this.addComment("After.Register", "The actions that will be executed after the user registers");
        this.add("After.Login", Utils.toList("msg:&aGreat! Now you can play", "cmd:kit nooby", "server:lobby"));
        this.addComment("After.Login", "The actions that will be executed after the user logs in");
        this.add("WhitelistedCommands", Utils.toList("register", "login"));
        this.addComment("WhitelistedCommands", "The commands that can be executed even if the user is not logged in");
        this.add("Title.After.Login", "&bLogged In!");
        this.addComment("Title.After.Login", "The title that will be shown after the user logs in");
        this.add("Subtitle.After.Login", "&7Now you can play!");
        this.addComment("Subtitle.After.Login", "The subtitle that will be shown after the user logs in");
        this.add("Subtitle.After.Register", "&7Thank you for choosing us!");
        this.addComment("Subtitle.After.Register", "The subtitle that will be shown after the user registers");
        this.add("Title.After.Register", "&bRegistered In!");
        this.addComment("Title.After.Register", "The title that will be shown after the user registers");
        this.add("Title-Time.After.Login", "10;20;10");
        this.addComment("Title-Time.After.Login", "The timing in ticks that the title will be shown (in;stay;out)");
        this.add("Title-Time.After.Register", "10;20;10");
        this.addComment("Title-Time.After.Register", "The timing in ticks that the title will be shown (in;stay;out)");
        this.add("BlockedActions", Utils.toList("BLOCK_BREAK", "BLOCK_PLACE", "CHAT", "MOVEMENT", "INTERACTION", "CUSTOM_INVENTORY", "DAMAGE", "ITEM_DROP"));
        this.addComment("BlockedActions", "The actions that will be blocked if the user is not logged in");
        this.add("Auth.CommandUsageTimer", 3);
        this.addComment("Auth.CommandUsageTimer", "Period of time in seconds that will be waited before sending again how to login");
        this.add("Auth.PremiumAutoLogin", true);
        this.addComment("Auth.PremiumAutoLogin", "Should the premium players be automatically logged in?");
        this.add("Auth.Sessions.Enabled", true);
        this.addComment("Auth.Sessions.Enabled", "Should the sessions be enabled?");
        this.add("Auth.Sessions.MaxTime", 300);
        this.addComment("Auth.Sessions.MaxTime", "The max time (in seconds) that the user can have an active session.");
        this.checkTimings();
    }

    @Override
    public void load() {
        super.load();
        this.authMethod = AuthMethod.of(this.getString("AuthMethod"));
    }

    public boolean isVPNBlocker(){
        return this.getBoolean("AntiBots.VPNBlocker", false);
    }

    public boolean isBlockIPChanges(){
        return this.getBoolean("AntiBots.BlockIPChanges", true);
    }

    public int getSessionMaxTime(){
        return this.getInt("Auth.Sessions.MaxTime", 300);
    }

    public boolean isSessionsEnabled(){
        return this.getBoolean("Auth.Sessions.Enabled", true);
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
