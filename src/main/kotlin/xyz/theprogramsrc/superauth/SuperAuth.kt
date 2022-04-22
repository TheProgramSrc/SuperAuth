package xyz.theprogramsrc.superauth

import org.bukkit.plugin.java.JavaPlugin
import xyz.theprogramsrc.superauth.api.SuperAuthAPI

class SuperAuth: JavaPlugin(), SuperAuthAPI {

    companion object {
        lateinit var instance: SuperAuthAPI
            private set
    }

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {

    }

}