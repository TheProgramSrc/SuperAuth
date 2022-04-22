package xyz.theprogramsrc.superauth_v3.global;

import xyz.theprogramsrc.supercoreapi.SuperPlugin;
import xyz.theprogramsrc.supercoreapi.global.storage.memory.MemoryStorage;

public class SessionStorage extends MemoryStorage<String> {

    public static SessionStorage i;

    public SessionStorage(SuperPlugin<?> plugin) {
        super(plugin);
        i = this;
    }

}
