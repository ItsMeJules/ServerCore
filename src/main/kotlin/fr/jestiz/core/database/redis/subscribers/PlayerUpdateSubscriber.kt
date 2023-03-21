package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.players.PlayerManager
import java.util.*

// Gets the latest update of the player record if it's cached in the plugin.
object PlayerUpdateSubscriber : RedisSubscriber(Constants.REDIS_PLAYER_UPDATE_CHANNEL) {

    override fun subscribe() {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            val serverUUID = UUID.fromString(jsonObject["server-id"]!!.asString) // This can't be null
            if (serverUUID == Core.serverID)
                return@parser

            val uuid = UUID.fromString(jsonObject["uuid"]!!.asString) // This can't be null
            if (!PlayerManager.offlinePlayerExists(uuid) && !PlayerManager.onlinePlayerExists(uuid))
                return@parser

            RedisServer.runCommand { PlayerManager.getOfflinePlayer(uuid).load(it) }
        }
        super.subscribe()
    }

}