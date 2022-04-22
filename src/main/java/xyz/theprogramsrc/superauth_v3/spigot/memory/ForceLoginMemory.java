package xyz.theprogramsrc.superauth_v3.spigot.memory;

import xyz.theprogramsrc.superauth_v3.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class ForceLoginMemory extends MemoryStorage<String> {

    public static ForceLoginMemory i;

    public ForceLoginMemory() {
        super(SuperAuth.spigot);
        i = this;
    }
}
