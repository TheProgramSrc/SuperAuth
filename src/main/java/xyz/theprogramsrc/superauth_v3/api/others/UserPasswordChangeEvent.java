package xyz.theprogramsrc.superauth_v3.api.others;

import xyz.theprogramsrc.superauth_v3.api.SuperAuthAPIEvent;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.spigot.storage.AuthSettings;

public class UserPasswordChangeEvent extends SuperAuthAPIEvent {
    public UserPasswordChangeEvent(AuthSettings authSettings, UserStorage userStorage, String user) {
        super(authSettings, userStorage, user);
    }
}
