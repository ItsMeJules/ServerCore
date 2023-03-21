package fr.jestiz.core

import com.google.gson.JsonObject
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.fancymessage.FancyMessage
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit

class Broadcaster {

    private var mustNotHave = false
    private var permission: String? = null

    fun viewPermission(permission: String): Broadcaster {
        this.permission = permission
        return this
    }

    fun mustHave(): Broadcaster {
        mustNotHave = false
        return this
    }

    fun mustNotHave(): Broadcaster {
        mustNotHave = true
        return this
    }

    fun broadCastNetwork(fancyMessage: FancyMessage) {
        RedisServer.publish(Constants.REDIS_BROADCAST_CHANNEL) {
            val json = JsonObject()

            json.addProperty("type", "fancy-message")
            json.addProperty("message", fancyMessage.msg)

            if (permission != null) {
                json.addProperty("permission", permission)
                json.addProperty("must-have-permission", !mustNotHave)
            }

            fancyMessage.clickAction?.let { json.addProperty("click", it.name) }
            fancyMessage.clickMessage?.let { json.addProperty("click-message", it) }
            fancyMessage.hoverAction?.let { json.addProperty("hover", it.name) }
            fancyMessage.hoverMessage?.let { json.addProperty("hover-message", it) }

            return@publish json
        }
        broadcastServer(fancyMessage)
    }

    fun broadcastServer(fancyMessage: FancyMessage) {
        var built = fancyMessage.build()
        val builder = StringBuilder().append(built.map{ it.toPlainText() })

        if (permission == null)
            PlayerManager.getOnlinePlayers().forEach { it.player.spigot().sendMessage(*built) }
        else if (mustNotHave) {
                PlayerManager.getOnlinePlayers()
                    .filterNot { it.player.hasPermission(permission) }
                    .forEach { it.player.spigot().sendMessage(*built) }
        } else {
            PlayerManager.getOnlinePlayers()
                .filter { it.player.hasPermission(permission) }
                .forEach { it.player.spigot().sendMessage(*built) }
            Bukkit.getConsoleSender().sendMessage(builder.toString()) // I should set the right colors to the console.
        }
    }

}