package xyz.theprogramsrc.superauth.spigot.memory;

import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class WasRegisteredMemory extends MemoryStorage<Boolean> {

    public static WasRegisteredMemory i;

    public WasRegisteredMemory() {
        super(SuperAuth.spigot);
        i = this;
    }
}
