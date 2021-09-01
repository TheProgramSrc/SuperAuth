package xyz.theprogramsrc.superauth.global.users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.supercoreapi.SuperPlugin;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBaseStorage;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

public class UserStorage extends DataBaseStorage {

    private final String table;
    private final HashMap<String, User> cache;

    public UserStorage(SuperPlugin<?> plugin, DataBase dataBase) {
        super(plugin, dataBase);
        this.cache = new HashMap<>();
        this.table = this.getTablePrefix() + "users";
        this.preloadTables();
    }

    public SuperPlugin<?> getPlugin() {
        return plugin;
    }

    public DataBase getDataBase() {
        return dataBase;
    }

    public void saveAndGet(User user, Consumer<User> then){
        this.save(user, () -> {
            this.get(user.getUsername(), then);
        });
    }

    public void save(User user){
        this.save(user, null);
    }

    public void save(final User user, Runnable then){
        this.dataBase.connect(c-> saveUser(user, c, then, ex -> {
            this.plugin.addError(ex);
            this.plugin.log("&c" + LBase.ERROR_WHILE_SAVING_USER_DATA.options().vars(user.getUsername()).placeholder("{UserName}", user.getUsername()).toString());
            ex.printStackTrace();
        }));
    }

    public void saveUser(User user, Connection c, Runnable then, Consumer<Exception> error){
        String username = user.getUsername();
        this.exists(username, exists -> {
            try{
                String password = user.getPassword();
                String ip = user.getIp();
                String authMethod = user.getAuthMethod();
                String skin_texture;
                if(user.hasSkin()){
                    skin_texture = Utils.encodeBase64(user.getSkinTexture());
                }else{
                    skin_texture = "no_skin";
                }
                int premium = user.isPremium() ? 1 : 0;
                int admin = user.isAdmin() ? 1 : 0;
                int authorized = user.isAuthorized() ? 1 : 0;
                int registered = user.isRegistered() ? 1 : 0;
                PreparedStatement preparedStatement;
                if(!exists){
                    preparedStatement = c.prepareStatement("INSERT INTO " + this.table + " (username, password, ip, auth_method, skin_texture, premium, admin, authorized, registered) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setString(1, username);
                    preparedStatement.setString(2, password);
                    preparedStatement.setString(3, ip);
                    preparedStatement.setString(4, authMethod);
                    preparedStatement.setString(5, skin_texture);
                    preparedStatement.setInt(6, premium);
                    preparedStatement.setInt(7, admin);
                    preparedStatement.setInt(8, authorized);
                    preparedStatement.setInt(9, registered);
                }else{
                    preparedStatement = c.prepareStatement("UPDATE " + this.table + " SET password = ?, ip = ?, auth_method = ?, skin_texture = ?, premium = ?, admin = ?, authorized = ?, registered = ? WHERE username = ?");
                    preparedStatement.setString(1, password);
                    preparedStatement.setString(2, ip);
                    preparedStatement.setString(3, authMethod);
                    preparedStatement.setString(4, skin_texture);
                    preparedStatement.setInt(5, premium);
                    preparedStatement.setInt(6, admin);
                    preparedStatement.setInt(7, authorized);
                    preparedStatement.setInt(8, registered);
                    preparedStatement.setString(9, username);
                }
                preparedStatement.executeUpdate();
                preparedStatement.close();
                this.cache.put(user.getUsername(), user);
                if(then != null) then.run();
            }catch(SQLException ex){
                if(error != null) error.accept(ex);
                this.removeCache(user.getUsername());
            }
        });
    }

    public void get(String username, Consumer<User> then){
        this.get(username, false, then);
    }

    public void get(final String username, boolean override_cache, Consumer<User> then){
        if(!override_cache){
            if(this.cache.containsKey(username) && then != null){
                then.accept(this.cache.get(username));
                return;
            }
        }
        this.dataBase.connect(c->{
            try{
                PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table + " WHERE username = ?");
                preparedStatement.setString(1, username);
                ResultSet rs = preparedStatement.executeQuery();
                if(rs.next()){
                    String password = rs.getString("user_password");
                    String ip = rs.getString("user_ip");
                    String authMethod = rs.getString("auth_method");
                    boolean premium = rs.getBoolean("is_premium");
                    boolean admin = rs.getBoolean("is_admin");
                    boolean authorized = rs.getBoolean("is_authorized");
                    boolean registered = rs.getBoolean("is_registered");
                    String skinTexture;
                    if(rs.getString("skin_texture").equals("no_skin")){
                        skinTexture = null;
                    }else{
                        skinTexture = Utils.decodeBase64(rs.getString("skin_texture"));
                    }
                    User user = new User(username)
                        .setSkinTexture(skinTexture)
                        .setRegistered(registered)
                        .setAuthorized(authorized)
                        .setPassword(password)
                        .setIp(ip)
                        .setPremium(premium)
                        .setAdmin(admin)
                        .setAuthMethod(authMethod);
                    this.cache.put(username, user);
                    if(then != null){
                        then.accept(user);
                    }
                }else{
                    if(then != null) then.accept(null);
                }
                rs.close();
                preparedStatement.close();
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
                if(then != null) then.accept(null);
            }
        });
    }

    public void isUserAuthorized(String username, Consumer<Boolean> then){
        this.isUserAuthorized(username, false, then);  
    }

    public void isUserAuthorized(String username, boolean override_cache, Consumer<Boolean> then){
        if(!override_cache){
            if(this.cache.containsKey(username) && then != null){
                then.accept(this.cache.get(username).isAuthorized());
                return;
            }
        }

        this.dataBase.connect(c->{
            try{
                PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table + " WHERE username = ?");
                preparedStatement.setString(1, username);
                ResultSet rs = preparedStatement.executeQuery();
                if(rs.next()){
                    boolean authorized = rs.getBoolean("is_authorized");
                    if(then != null){
                        then.accept(authorized);
                    }
                }else{
                    if(then != null) then.accept(false);
                }
                rs.close();
                preparedStatement.close();
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
                if(then != null) then.accept(false);
            }
        });
    }

    public void requestUsers(Consumer<User[]> then){
        requestUsers(false, then);
    }

    public void requestUsers(boolean overrideCache, Consumer<User[]> then){
        if(!overrideCache){
            then.accept(this.cache.values().stream().filter(Utils::nonNull).toArray(User[]::new));
            return;
        }
        
        this.dataBase.connect(c->{
            LinkedList<User> users = new LinkedList<>();
            try{
                PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table);
                ResultSet rs = preparedStatement.executeQuery();
                while(rs.next()){
                    String username = rs.getString("user_name");
                    this.cache.remove(username);
                    String ip = rs.getString("user_ip");
                    String password = rs.getString("user_password");
                    boolean premium = rs.getBoolean("is_premium");
                    boolean admin = rs.getBoolean("is_admin");
                    boolean authorized = rs.getBoolean("is_authorized");
                    boolean registered = rs.getBoolean("is_registered");
                    String authMethod = rs.getString("auth_method");
                    String skinTexture;
                    if(rs.getString("skin_texture").equals("no_skin")){
                        skinTexture = null;
                    }else{
                        skinTexture = Utils.decodeBase64(rs.getString("skin_texture"));
                    }
                    User user = new User(username)
                            .setSkinTexture(skinTexture)
                            .setRegistered(registered)
                            .setAuthorized(authorized)
                            .setPassword(password)
                            .setIp(ip)
                            .setPremium(premium)
                            .setAdmin(admin)
                            .setAuthMethod(authMethod);
                    this.cache.put(username, user);
                    users.add(user);
                }
                rs.close();
                preparedStatement.close();
            }catch (Exception ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
            }
            if(then != null){
                then.accept(users.stream().filter(Utils::nonNull).toArray(User[]::new));
            }
        });
    }

    public void exists(String username, Consumer<Boolean> then){
        this.dataBase.connect(c->{
            try{
                PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table + " WHERE username = ?");
                preparedStatement.setString(1, username);
                ResultSet rs = preparedStatement.executeQuery();
                if(then != null) then.accept(rs.next());
                rs.close();
                preparedStatement.close();
            }catch (Exception ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
                if(then != null) then.accept(false);;
            }
        });
    }

    private void preloadTables(){
        this.dataBase.connect(c->{
            try{
                PreparedStatement preparedStatement = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.table + " (user_name VARCHAR(100), user_password MEDIUMTEXT, user_ip MEDIUMTEXT, is_premium INT, is_admin INT, is_authorized INT, is_registered INT, auth_method VARCHAR(100), skin_texture VARCHAR(500));");
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_WHILE_CREATING_TABLES);
                ex.printStackTrace();
            }
        });
    }

    public void getRandomUser(Consumer<User> then){
        this.requestUsers(users -> {
            then.accept(users[Utils.random(0, users.length)]);
        });
    }

    public void getRandomUserWithSkin(Consumer<User> then){
        this.requestUsers(users -> {
            if(users.length == 0){
                if(then != null) then.accept(null);
            }else{
                int rand = Utils.random(0, users.length);
                User user = null;
                while(!(user = users[rand]).hasSkin()){
                    rand = Utils.random(0, users.length);
                }
                if(then != null) then.accept(user);
            }
        });
    }

    public void remove(User user, Runnable then) {
        this.dataBase.connect(c->{
            try{
                PreparedStatement preparedStatement = c.prepareStatement("DELETE FROM " + this.table + " WHERE username = ?");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                this.removeCache(user.getUsername());
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_WHILE_DELETING_USER);
                ex.printStackTrace();
            }
            if(then != null) then.run();
        });
    }

    public void removeCache(String username){
        this.cache.remove(username);
    }
}
