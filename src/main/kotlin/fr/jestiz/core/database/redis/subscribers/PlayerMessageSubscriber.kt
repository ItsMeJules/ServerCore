package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.players.PlayerManager
import java.util.*

object PlayerMessageSubscriber : RedisSubscriber(Constants.REDIS_PLAYER_MESSAGE_CHANNEL) {

    override fun subscribe() {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            if (isServerSender(jsonObject))
                return@parser

            val uuid = UUID.fromString(jsonObject["uuid"]!!.asString) // This can't be null
            if (!PlayerManager.onlinePlayerExists(uuid))
                return@parser

            PlayerManager.getOnlinePlayer(uuid).player.sendMessage(jsonObject["message"]!!.asString)
        }
    }

}