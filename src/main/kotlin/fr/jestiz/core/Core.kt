package fr.jestiz.core

import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.punishments.Ban
import fr.jestiz.core.punishments.Punishment
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Core : JavaPlugin() {

    override fun onEnable() {
        instance = this
        println("subscribed")
        Configurations.createResources()
        Punishment.subscribe()
        Thread.sleep(1000L)
        var ban = Ban(UUID.randomUUID(), UUID.randomUUID())
        println("issued")
        ban.add()
    }

    companion object {
        lateinit var instance: Core
    }

}