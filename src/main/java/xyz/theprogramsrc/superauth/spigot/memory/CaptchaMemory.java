package xyz.theprogramsrc.superauth.spigot.memory;

import xyz.theprogramsrc.superauth.global.users.User;
import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class CaptchaMemory extends MemoryStorage<User> {

    public static CaptchaMemory i;

    public CaptchaMemory() {
        super(SuperAuth.spigot);
        i = this;
    }
}
