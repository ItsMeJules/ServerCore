package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Broadcaster
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.fancymessage.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import java.lang.RuntimeException

class BroadcastSubscriber : RedisSubscriber(Constants.REDIS_BROADCAST_CHANNEL) {

    init {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            if (isServerSender(jsonObject))
                return@parser

            handleMessage(jsonObject)
        }
    }

    private fun handleMessage(jsonObject: JsonObject) {
        val broadcast = Broadcaster()

        jsonObject["permission"]?.let {
            broadcast.viewPermission(it.asString)

            if (jsonObject["must-have-permission"].asBoolean)
                broadcast.mustHave()
            else
                broadcast.mustNotHave()
        }

        jsonObject["type"]?.let { type ->
            when (type.asString) {
                // message property is always present.
                "fancy-message" -> buildFancyMessage(broadcast, jsonObject["message"]!!.asString, jsonObject)
                "normal-message" -> buildNormalMessage(broadcast, jsonObject["message"]!!.asString, jsonObject)
                else -> throw RuntimeException("Message type '$type' not handled!")
            }
        }
    }

     private fun buildFancyMessage(broadcaster: Broadcaster, message: String, jsonObject: JsonObject) {
         val fancyMessage = FancyMessage(message)

         jsonObject["click"]?.let { fancyMessage.clickEvent(ClickEvent.Action.valueOf(it.asString)) }
         jsonObject["click-message"]?.let { fancyMessage.click(it.asString) }
         jsonObject["hover"]?.let { fancyMessage.hoverEvent(HoverEvent.Action.valueOf(it.asString)) }
         jsonObject["hover-message"]?.let { fancyMessage.hover(it.asString) }

         broadcaster.broadcastServer(fancyMessage)
     }

    private fun buildNormalMessage(broadcaster: Broadcaster, message: String, jsonObject: JsonObject) {

    }

}