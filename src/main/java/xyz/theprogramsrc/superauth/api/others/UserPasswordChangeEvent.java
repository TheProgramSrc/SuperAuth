package xyz.theprogramsrc.superauth.api.others;

import xyz.theprogramsrc.superauth.api.SuperAuthAPIEvent;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;

public class UserPasswordChangeEvent extends SuperAuthAPIEvent {
    public UserPasswordChangeEvent(AuthSettings authSettings, UserStorage userStorage, String user) {
        super(authSettings, userStorage, user);
    }
}
