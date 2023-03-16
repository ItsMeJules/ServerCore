package fr.jestiz.core

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

        createResources()
        registerListeners()

        Punishment.subscribe()
        CommandHandler.registerParameterProcessors("fr.jestiz.core.api.commands.parameters.processors.defaults")
        CommandHandler.registerCommands("fr.jestiz.core.commands")
    }

    override fun onDisable() {
        PlayerManager.saveServerPlayers()
        RedisServer.closeConnections()
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

    companion object {
        lateinit var instance: Core

        fun broadcastWithPerm(fancyMessage: FancyMessage, permission: String) {
            var built = fancyMessage.build()
            val builder = StringBuilder().append(built.map{ it.toPlainText() })

            PlayerManager.getOnlinePlayers()
                .filter { it.player.hasPermission(permission) }
                .forEach { it.player.spigot().sendMessage(*built) }

            Bukkit.getConsoleSender().sendMessage(builder.toString()) // I should set the right colors to the console.
        }

        //no need to send to the console as it has got all permissions.
        fun broadcastWithoutPerm(fancyMessage: FancyMessage, permission: String) {
            var built = fancyMessage.build()

            PlayerManager.getOnlinePlayers()
                .filterNot { it.player.hasPermission(permission) }
                .forEach { it.player.spigot().sendMessage(*built) }
        }
    }

}