package xyz.theprogramsrc.superauth.global.users;

import xyz.theprogramsrc.superauth.global.languages.LBase;
import xyz.theprogramsrc.supercoreapi.SuperPlugin;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBase;
import xyz.theprogramsrc.supercoreapi.global.storage.DataBaseStorage;
import xyz.theprogramsrc.supercoreapi.global.utils.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

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

    public void saveAndGet(User user, Consumer<User> then) {
        this.save(user, () -> this.get(user.getUsername(), then));
    }

    public void save(User user) {
        this.save(user, null);
    }

    public void save(final User user, Runnable then) {
        this.dataBase.connect(c -> saveUser(user, c, then, ex -> {
            this.plugin.addError(ex);
            this.plugin.log("&c" + LBase.ERROR_WHILE_SAVING_USER_DATA.options().vars(user.getUsername())
                    .placeholder("{UserName}", user.getUsername()).toString());
            ex.printStackTrace();
        }));
    }

    public void saveUser(User user, Connection c, Runnable then, Consumer<Exception> error) {
        String username = user.getUsername();
        this.exists(username, exists -> {
            String query = String.format(exists
                    ? "UPDATE %s SET user_password = ?, user_ip = ?, auth_method = ?, skin_texture = ?, is_premium = ?, is_admin = ?, is_authorized = ?, is_registered = ? WHERE user_name = ?"
                    : "INSERT INTO %s (user_password, user_ip, auth_method, skin_texture, is_premium, is_admin, is_authorized, is_registered, user_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    this.table);
            try (PreparedStatement preparedStatement = c.prepareStatement(query)) {
                preparedStatement.setString(1, user.getPassword());
                if(user.getIp() == null){
                    preparedStatement.setNull(2, java.sql.Types.VARCHAR);
                }else{
                    preparedStatement.setString(2, user.getIp());
                }
                preparedStatement.setString(3, user.getAuthMethod());
                preparedStatement.setString(4, user.hasSkin() ? Utils.encodeBase64(user.getSkinTexture()) : "no_skin");
                preparedStatement.setBoolean(5, user.isPremium());
                preparedStatement.setBoolean(6, user.isAdmin());
                preparedStatement.setBoolean(7, user.isAuthorized());
                preparedStatement.setBoolean(8, user.isRegistered());
                preparedStatement.setString(9, username);
                preparedStatement.executeUpdate();
                this.cache.put(user.getUsername(), user);
                if (then != null)
                    then.run();
            } catch (SQLException ex) {
                if (error != null)
                    error.accept(ex);
                this.removeCache(user.getUsername());
            }
        });
    }

    public void get(String username, Consumer<User> then) {
        this.get(username, false, then);
    }

    public void get(final String username, boolean overrideCache, Consumer<User> then) {
        if (!overrideCache && this.cache.containsKey(username) && then != null) {
            then.accept(this.cache.get(username));
            return;
        }
        this.dataBase.connect(c -> {
            try(PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table + " WHERE user_name = ?")){
                preparedStatement.setString(1, username);
                try(ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        String password = rs.getString("user_password");
                        String ip = rs.getString("user_ip");
                        String authMethod = rs.getString("auth_method");
                        boolean premium = rs.getBoolean("is_premium");
                        boolean admin = rs.getBoolean("is_admin");
                        boolean authorized = rs.getBoolean("is_authorized");
                        boolean registered = rs.getBoolean("is_registered");
                        String skinTexture;
                        if (rs.getString("skin_texture").equals("no_skin")) {
                            skinTexture = null;
                        } else {
                            skinTexture = Utils.decodeBase64(rs.getString("skin_texture"));
                        }
                        User user = new User(username).setSkinTexture(skinTexture).setRegistered(registered)
                                .setAuthorized(authorized).setPassword(password).setIp(ip).setPremium(premium)
                                .setAdmin(admin).setAuthMethod(authMethod);
                        this.cache.put(username, user);
                        if (then != null) {
                            then.accept(user);
                        }
                    } else {
                        if (then != null)
                            then.accept(null);
                    }
                }
            } catch (SQLException ex) {
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
                if (then != null)
                    then.accept(null);
            }
        });
    }

    public void isUserAuthorized(String username, Consumer<Boolean> then) {
        this.isUserAuthorized(username, false, then);
    }

    public void isUserAuthorized(String username, boolean override_cache, Consumer<Boolean> then) {
        if (!override_cache) {
            if (this.cache.containsKey(username) && then != null) {
                then.accept(this.cache.get(username).isAuthorized());
                return;
            }
        }

        this.dataBase.connect(c -> {
            try (PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table + " WHERE user_name = ?")){
                preparedStatement.setString(1, username);
                try(ResultSet rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        boolean authorized = rs.getBoolean("is_authorized");
                        if (then != null) {
                            then.accept(authorized);
                        }
                    } else {
                        if (then != null)
                            then.accept(false);
                    }
                }
            } catch (SQLException ex) {
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
                if (then != null)
                    then.accept(false);
            }
        });
    }

    public void requestUsers(Consumer<User[]> then) {
        requestUsers(false, then);
    }

    public void requestUsers(boolean overrideCache, Consumer<User[]> then) {
        if (!overrideCache) {
            then.accept(this.cache.values().stream().filter(Utils::nonNull).toArray(User[]::new));
            return;
        }

        this.dataBase.connect(c -> {
            LinkedList<User> users = new LinkedList<>();
            try (PreparedStatement preparedStatement = c.prepareStatement("SELECT * FROM " + this.table); ResultSet rs = preparedStatement.executeQuery()){
                while (rs.next()) {
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
                    if (rs.getString("skin_texture").equals("no_skin")) {
                        skinTexture = null;
                    } else {
                        skinTexture = Utils.decodeBase64(rs.getString("skin_texture"));
                    }
                    User user = new User(username).setSkinTexture(skinTexture).setRegistered(registered)
                            .setAuthorized(authorized).setPassword(password).setIp(ip).setPremium(premium)
                            .setAdmin(admin).setAuthMethod(authMethod);
                    this.cache.put(username, user);
                    users.add(user);
                }
            } catch (Exception ex) {
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
            }
            if (then != null) {
                then.accept(users.stream().filter(Utils::nonNull).toArray(User[]::new));
            }
        });
    }

    public void exists(String username, Consumer<Boolean> then, Consumer<Exception> error) {
        this.dataBase.connect(c -> {
            try(PreparedStatement preparedStatement = c.prepareStatement("SELECT COUNT(*) FROM " + this.table + " WHERE user_name = ?;")){
                preparedStatement.setString(1, username);
                try(ResultSet rs = preparedStatement.executeQuery()) {
                    if (then != null) {
                        then.accept(rs.next() && rs.getInt(1) > 0);
                    }
                }
            } catch (Exception ex) {
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_ON_DATA_REQUEST);
                ex.printStackTrace();
                if (then != null)
                    then.accept(false);
                if (error != null)
                    error.accept(ex);
            }
        });
    }

    public void exists(String username, Consumer<Boolean> then) {
        this.exists(username, then, null);
    }

    private void preloadTables() {
        this.dataBase.connect(c -> {
            try(PreparedStatement preparedStatement = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.table + " (user_name VARCHAR(100), user_password MEDIUMTEXT, user_ip MEDIUMTEXT, is_premium INT, is_admin INT, is_authorized INT, is_registered INT, auth_method VARCHAR(100), skin_texture VARCHAR(500));")){
                preparedStatement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_WHILE_CREATING_TABLES);
                ex.printStackTrace();
            }
        });
    }

    public void getRandomUser(Consumer<User> then) {
        this.requestUsers(users -> then.accept(users[Utils.random(0, users.length)]));
    }

    public void getRandomUserWithSkin(Consumer<User> then) {
        if(then != null){
            this.requestUsers(users -> {
                if (users.length == 0) {
                    then.accept(null);
                } else {
                    User user = null;
                    try{
                        if(users.length > 1){
                            while(!(user = users[Utils.random(0, users.length)]).hasSkin()){
                                user = users[Utils.random(0, users.length)];
                            }
                        }else{
                            user = users[0];
                        }
                    }catch(Exception ignored){} // We ignore the exception
                    then.accept(user != null ? (user.hasSkin() ? user : null) : null);
                }
            });
        }
    }

    public void remove(User user, Runnable then) {
        this.dataBase.connect(c -> {
            try {
                PreparedStatement preparedStatement = c
                        .prepareStatement("DELETE FROM " + this.table + " WHERE user_name = ?");
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.executeUpdate();
                preparedStatement.close();
                this.removeCache(user.getUsername());
            } catch (SQLException ex) {
                this.plugin.addError(ex);
                this.plugin.log("&c" + LBase.ERROR_WHILE_DELETING_USER);
                ex.printStackTrace();
            }
            if (then != null)
                then.run();
        });
    }

    public void removeCache(String username) {
        this.cache.remove(username);
    }
}
