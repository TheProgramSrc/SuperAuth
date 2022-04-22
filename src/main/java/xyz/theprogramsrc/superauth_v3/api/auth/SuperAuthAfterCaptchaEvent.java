package xyz.theprogramsrc.superauth_v3.api.auth;

import xyz.theprogramsrc.superauth_v3.api.SuperAuthAPIEvent;
import xyz.theprogramsrc.superauth_v3.global.users.UserStorage;
import xyz.theprogramsrc.superauth_v3.spigot.storage.AuthSettings;

public class SuperAuthAfterCaptchaEvent extends SuperAuthAPIEvent {

    public SuperAuthAfterCaptchaEvent(AuthSettings authSettings, UserStorage userStorage, String user) {
        super(authSettings, userStorage, user);
    }

}
