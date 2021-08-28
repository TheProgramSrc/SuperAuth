package xyz.theprogramsrc.superauth.global.users;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
                Statement s = c.createStatement();
                if(!exists){
                    s.executeUpdate("INSERT INTO " + this.table + " (user_name, user_password, user_ip, is_premium, is_admin, is_authorized, is_registered, auth_method, skin_texture) VALUES ('"+username+"', '"+password+"', '"+ip+"', '"+premium+"', '"+admin+"', '"+authorized+"', '"+registered+"', '"+authMethod+"', '"+skin_texture+"');");
                }else{
                    s.executeUpdate("UPDATE " + this.table + " SET user_password='"+password+"', user_ip='"+ip+"', is_premium='"+premium+"', is_admin='"+admin+"', is_authorized='"+authorized+"', is_registered='"+registered+"', auth_method='"+authMethod+"', skin_texture='"+skin_texture+"' WHERE user_name='"+username+"';");
                }
                s.close();
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
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM " + this.table + " WHERE user_name='"+username+"';");
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
                s.close();
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
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT is_authorized FROM " + this.table + " WHERE user_name='"+username+"';");
                if(rs.next()){
                    boolean authorized = rs.getBoolean("is_authorized");
                    if(then != null){
                        then.accept(authorized);
                    }
                }else{
                    if(then != null) then.accept(false);
                }
                rs.close();
                s.close();
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
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM " + this.table + ";");
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
                s.close();
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
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM " + this.table + " WHERE user_name='"+username+"';");
                if(then != null) then.accept(rs.next());
                rs.close();
                s.close();
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
                Statement s = c.createStatement();
                s.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.table + " (user_name VARCHAR(100), user_password MEDIUMTEXT, user_ip MEDIUMTEXT, is_premium INT, is_admin INT, is_authorized INT, is_registered INT, auth_method VARCHAR(100), skin_texture VARCHAR(500));");
                s.close();
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
                Statement s = c.createStatement();
                s.executeUpdate("DELETE FROM " + this.table + " WHERE user_name='"+user.getUsername()+"'");
                s.close();
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
