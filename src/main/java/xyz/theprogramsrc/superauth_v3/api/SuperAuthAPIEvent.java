package xyz.theprogramsrc.superauth_v3.api;

import xyz.theprogramsrc.superauth_v3.global.hashing.HashingMethod;
import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.spigot.storage.AuthSettings;

public class SuperAuthAPIEvent {

    private final UserStorage userStorage;
    private final AuthSettings authSettings;
    private User user;

    public SuperAuthAPIEvent(AuthSettings authSettings, UserStorage userStorage, String user) {
        this.authSettings = authSettings;
        this.userStorage = userStorage;
        userStorage.get(user, u -> this.user = u);
    }

    public AuthSettings getAuthSettings() {
        return authSettings;
    }

    public HashingMethod getHashingMethod(){
        return this.getAuthSettings().getHashingMethod();
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public User getUser() {
        return user;
    }
}
