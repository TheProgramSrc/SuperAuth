package xyz.theprogramsrc.superauth_v3.spigot.memory;

import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class WasRegisteredMemory extends MemoryStorage<Boolean> {

    public static WasRegisteredMemory i;

    public WasRegisteredMemory() {
        super(SuperAuth.spigot);
        i = this;
    }
}
