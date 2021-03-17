package xyz.theprogramsrc.superauth.spigot.memory;

import xyz.theprogramsrc.superauth.spigot.SuperAuth;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class ForceLoginMemory extends MemoryStorage<String> {

    public static ForceLoginMemory i;

    public ForceLoginMemory() {
        super(SuperAuth.spigot);
        i = this;
    }
}
