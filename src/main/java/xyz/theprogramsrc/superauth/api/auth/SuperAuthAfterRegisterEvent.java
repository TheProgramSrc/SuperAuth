package xyz.theprogramsrc.superauth.api.auth;

import xyz.theprogramsrc.superauth.api.SuperAuthAPIEvent;
import xyz.theprogramsrc.superauth.global.users.UserStorage;
import xyz.theprogramsrc.superauth.spigot.storage.AuthSettings;

public class SuperAuthAfterRegisterEvent extends SuperAuthAPIEvent {

    public SuperAuthAfterRegisterEvent(AuthSettings authSettings, UserStorage userStorage, String user) {
        super(authSettings, userStorage, user);
    }

}
