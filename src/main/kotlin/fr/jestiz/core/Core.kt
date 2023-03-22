package fr.jestiz.core

import com.google.gson.JsonObject
import fr.jestiz.core.api.commands.CommandHandler
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.fancymessage.FancyMessage
import fr.jestiz.core.listeners.ServerPlayerListener
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.punishments.Punishment
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class Core : JavaPlugin() {

    override fun onEnable() {
        instance = this
        serverID = UUID.randomUUID()

        createResources()
        registerListeners()

        CommandHandler.registerParameterProcessors("fr.jestiz.core.api.commands.parameters.processors.defaults")
        CommandHandler.registerCommands("fr.jestiz.core.commands")

        RedisServer.connect()
        RedisServer.publish(Constants.REDIS_SERVER_HEARTBEAT_CHANNEL, ::publishStarted)
        Punishment.init()
    }

    private fun publishStarted(jsonObject: JsonObject) {
        jsonObject.addProperty("status", "started")
    }

    private fun createResources() {
        Configurations.copyDefaultResource("config.yml")
        Configurations.copyDefaultResource("messages.yml")
        Configurations.copyDefaultResource("redis.yml")
    }

    private fun registerListeners() {
        Bukkit.getPluginManager().apply {
            registerEvents(ServerPlayerListener(), this@Core)
        }
    }

    override fun onDisable() {
        Punishment.saveIDs()

        RedisServer.publish(Constants.REDIS_SERVER_HEARTBEAT_CHANNEL, ::publishStop)
        RedisServer.closeConnections()
    }

    private fun publishStop(jsonObject: JsonObject) {
        jsonObject.addProperty("status", "stopped")
    }


    companion object {
        lateinit var instance: Core
        lateinit var serverID: UUID
    }

}