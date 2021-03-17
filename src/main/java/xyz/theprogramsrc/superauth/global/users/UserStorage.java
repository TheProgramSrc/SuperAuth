package xyz.theprogramsrc.superauth.global.users;

import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.supercoreapi.SuperPlugin;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBaseStorage;
import xyz.theprogramsrc.supercoreapi.global.storage.SQLiteDataBase;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

    public void save(final User user){
        new Thread(() -> {
            this.cache.remove(user.getUsername());
            this.dataBase.connect(c->{
                try{
                    String username = user.getUsername();
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
                    if(!this.exists(username)){
                        s.executeUpdate("INSERT INTO " + this.table + " (user_name, user_password, user_ip, is_premium, is_admin, is_authorized, is_registered, auth_method, skin_texture) VALUES ('"+username+"', '"+password+"', '"+ip+"', '"+premium+"', '"+admin+"', '"+authorized+"', '"+registered+"', '"+authMethod+"', '"+skin_texture+"');");
                    }else{
                        s.executeUpdate("UPDATE " + this.table + " SET user_password='"+password+"', user_ip='"+ip+"', is_premium='"+premium+"', is_admin='"+admin+"', is_authorized='"+authorized+"', is_registered='"+registered+"', auth_method='"+authMethod+"', skin_texture='"+skin_texture+"' WHERE user_name='"+username+"';");
                    }
                    s.closeOnCompletion();
                }catch (SQLException ex){
                    this.plugin.addError(ex);
                    this.plugin.log("&c" + LBase.ERROR_WHILE_SAVING_USER_DATA.options().vars(user.getUsername()).toString());
                    ex.printStackTrace();
                }
            });
        }).start();
    }

    public User get(String username){
        return get(username, false);
    }

    public User get(final String username, boolean override_cache){
        if(!override_cache){
            if(this.cache.containsKey(username)){
                return this.cache.get(username);
            }
        }

        final AtomicReference<User> result = new AtomicReference<>(null);
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
                    result.set(user);
                }
                s.closeOnCompletion();
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
            }
        });
        if(result.get() != null) this.cache.put(username, result.get());
        return result.get();
    }

    public User[] requestUsers(){
        return requestUsers(false);
    }

    public User[] requestUsers(boolean overrideCache){
        if(!overrideCache){
            return this.cache.values().stream().filter(Utils::nonNull).toArray(User[]::new);
        }else{
            List<User> users = new ArrayList<>();
            this.dataBase.connect(c->{
                try{
                    Statement s = c.createStatement();
                    ResultSet rs = s.executeQuery("SELECT * FROM " + this.table + ";");
                    while(rs.next()){
                        String username = rs.getString("user_name");
                        this.cache.remove(username);
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
                        users.add(user);
                    }
                }catch (Exception ex){
                    this.plugin.addError(ex);
                    this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                    ex.printStackTrace();
                }
            });
            if(!users.isEmpty()){
                User[] array = new User[users.size()];
                array = users.toArray(array);
                return array;
            }
        }
        return new User[0];
    }

    public boolean exists(String username){
        AtomicBoolean exists = new AtomicBoolean(false);
        this.dataBase.connect(c->{
            try{
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM " + this.table + " WHERE user_name='"+username+"';");
                exists.set(rs.next());
            }catch (Exception ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
            }
        });
        return exists.get();
    }

    private void preloadTables(){
        new Thread(() -> this.dataBase.connect(c->{
            try{
                Statement s = c.createStatement();
                s.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.table + " (user_name VARCHAR(100), user_password MEDIUMTEXT, user_ip MEDIUMTEXT, is_premium INT, is_admin INT, is_authorized INT, is_registered INT, auth_method VARCHAR(100), skin_texture VARCHAR(500));");
                s.closeOnCompletion();
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_WHILE_CREATING_TABLES);
                ex.printStackTrace();
            }

            // Fix old column
            try{
                Statement s = c.createStatement();
                if(this.dataBase instanceof SQLiteDataBase){
                    ResultSet rs = s.executeQuery("PRAGMA table_info(" + this.table + ");");
                    boolean migration = false;
                    while(rs.next()){
                        if(rs.getString("name").toLowerCase().equalsIgnoreCase("user_password")){
                            if(rs.getString("type").toLowerCase().contains("varchar")){
                                migration = true;
                                s.executeUpdate("CREATE TABLE IF NOT EXISTS tmp_" + this.table + " (user_name VARCHAR(100), user_password MEDIUMTEXT, user_ip MEDIUMTEXT, is_premium INT, is_admin INT, is_authorized INT, is_registered INT, auth_method VARCHAR(100), skin_texture VARCHAR(500));");
                                ResultSet res = s.executeQuery("SELECT * FROM " + this.table + ";");
                                while(res.next()){
                                    String username = res.getString("user_name");
                                    String password = res.getString("user_password");
                                    String ip = res.getString("user_ip");
                                    boolean premium = res.getBoolean("is_premium");
                                    boolean admin = res.getBoolean("is_admin");
                                    boolean authorized = res.getBoolean("is_authorized");
                                    boolean registered = res.getBoolean("is_registered");
                                    String authMethod = res.getString("auth_method");
                                    String skinTexture = res.getString("skin_texture");
                                    String update = String.format("INSERT INTO tmp_" + this.table + " (user_name, user_password, user_ip, is_premium, is_admin, is_authorized, is_registered, auth_method, skin_texture) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')", username, password, ip, premium, admin, authorized, registered, authMethod, skinTexture);
                                    s.executeUpdate(update);
                                }
                                res.close();
                            }
                        }
                    }
                    rs.close();
                    if(migration){
                        s.executeUpdate("DROP TABLE IF EXISTS " + this.table + ";");
                        s.executeUpdate("ALTER TABLE tmp_" + this.table + " RENAME TO " + this.table + ";");
                    }
                }else{
                    ResultSet rs = s.executeQuery("SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + this.table + "' AND COLUMN_NAME = 'user_password';");
                    if(rs.next()){
                        if(rs.getString("DATA_TYPE").equalsIgnoreCase("varchar")){
                            String update = "ALTER TABLE `" + this.table + "` MODIFY `user_password` MEDIUMTEXT NULL DEFAULT NULL;";
                            try{
                                s.executeUpdate(update);
                            }catch (SQLException ex){
                                if(ex.getMessage().toLowerCase().contains("access denied")){
                                    this.plugin.log("&cError while updating table.");
                                    this.plugin.log("&cPlease access to your MySQL Server with Admin Privileges and execute the following query:");
                                    this.plugin.log("&b&l" + update);
                                    this.plugin.log("&cIf you have a hosting please contact them and ask them to execute the query.");
                                }else{
                                    this.plugin.log("&c" + LBase.ERROR_WHILE_UPDATING_TABLES);
                                    this.plugin.addError(ex);
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
                s.closeOnCompletion();
            }catch (SQLException e){
                this.plugin.addError(e);
                this.plugin.log("&c" + LBase.ERROR_WHILE_UPDATING_TABLES);
                e.printStackTrace();
            }
        })).start();
    }

    public User getRandomUser(){
        User[] users = this.requestUsers();
        int rand = Utils.random(0, users.length);
        return users[rand];
    }

    public User getRandomUserWithSkin(){
        User[] users = this.requestUsers();
        if(users.length == 0){
            return null;
        }else{
            int rand = Utils.random(0, users.length);
            User user;
            while(!(user = users[rand]).hasSkin()){
                rand = Utils.random(0, users.length);
            }
            return user;
        }
    }

    public void remove(User user) {
        new Thread(()-> this.dataBase.connect(c->{
            try{
                Statement s = c.createStatement();
                s.executeUpdate("DELETE FROM " + this.table + " WHERE user_name='"+user.getUsername()+"'");
                s.closeOnCompletion();
                this.cache.remove(user.getUsername());
            }catch (SQLException ex){
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_WHILE_DELETING_USER);
                ex.printStackTrace();
            }
        })).start();
    }

    public void removeCache(String username) {
        this.cache.remove(username);
    }
}
