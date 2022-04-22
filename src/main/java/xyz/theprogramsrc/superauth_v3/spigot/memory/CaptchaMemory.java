package xyz.theprogramsrc.superauth_v3.spigot.memory;

import xyz.theprogramsrc.superauth_v3.global.users.User;
import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class CaptchaMemory extends MemoryStorage<User> {

    public static CaptchaMemory i;

    public CaptchaMemory() {
        super(SuperAuth.spigot);
        i = this;
    }
}
