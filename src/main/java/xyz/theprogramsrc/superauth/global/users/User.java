package xyz.theprogramsrc.superauth.global.users;

import xyz.theprogramsrc.superauth.global.hashing.Hashing;

import java.security.NoSuchAlgorithmException;

public class User {

    private final String username;
    private String password;
    private String ip;
    private boolean premium;
    private boolean admin;
    private boolean authorized;
    private boolean registered;
    private String authMethod;
    private String skinTexture;

    public User(String username){
        this.username = username;
        this.password = null;
        this.registered = false;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getIp() {
        return ip;
    }

    public boolean isPremium() {
        return premium;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public User setAuthMethod(String authMethod){
        this.authMethod = authMethod;
        return this;
    }

    public User setAuthorized(boolean authorized) {
        this.authorized = authorized;
        return this;
    }

    public User setPassword(String password){
        this.password = password;
        return this;
    }

    public User setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public User setPremium(boolean premium) {
        this.premium = premium;
        return this;
    }

    public User setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public User setRegistered(boolean registered) {
        this.registered = registered;
        return this;
    }

    public User setSkinTexture(String skinTexture){
        this.skinTexture = skinTexture;
        return this;
    }

    public boolean isValid(String password) throws NoSuchAlgorithmException {
        return Hashing.check(this.password, password);
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public boolean hasSkin() {
        if(this.skinTexture == null){
            return false;
        }else{
            return !this.skinTexture.equalsIgnoreCase("no_skin");
        }
    }
}
