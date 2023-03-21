package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import java.lang.RuntimeException

class BroadcastSubscriber : RedisSubscriber(Constants.REDIS_BROADCAST_CHANNEL) {

    init {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            if (isServerSender(jsonObject))
                return@parser

            jsonObject["type"]?.let { type ->
                when (type.asString) {
                    "fancy-message" -> handleFancyMessage(jsonObject)
                    "normal-message" -> handleNormalMessage(jsonObject)
                    else -> throw RuntimeException("Message type '$type' not handled!")
                }
            }
        }
    }

     private fun handleFancyMessage(jsonObject: JsonObject) {
        
     }

}