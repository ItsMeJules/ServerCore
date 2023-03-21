package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit
import java.util.*

// Gets the latest update of the player record if it's cached in the plugin.
class PlayerUpdateSubscriber : RedisSubscriber(Constants.REDIS_PLAYER_UPDATE_CHANNEL) {

    init {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            val serverIdString = jsonObject["server-id"]!!.asString // This can't be null
            if (serverIdString == Bukkit.getServerId())
                return@parser

            val uuid = UUID.fromString(jsonObject["uuid"]!!.asString) // This can't be null
            if (!PlayerManager.offlinePlayerExists(uuid) && !PlayerManager.onlinePlayerExists(uuid))
                return@parser

            RedisServer.runCommand { PlayerManager.getOfflinePlayer(uuid).load(it) }
        }
    }

}