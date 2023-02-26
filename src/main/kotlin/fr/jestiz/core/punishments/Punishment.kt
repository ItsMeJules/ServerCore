package fr.jestiz.core.punishments

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.redis.RedisServer
import fr.jestiz.core.redis.pubsub.RedisPublisher
import java.util.*

abstract class Punishment(private val sender: UUID, private val receiver: UUID): RedisPublisher {
    protected var id = 0
    var remover: UUID? = null
    var reason: String? = null
    var removeReason: String? = null
    val removed: Boolean
        get() = removeReason != null
    var silent = false
    var issued = System.currentTimeMillis()
    var expire = -1L
    var duration
        get() = expire - System.currentTimeMillis()
        set(duration) {
            expire = duration + System.currentTimeMillis()
        }

    val isActive: Boolean
        get() {
            return if (removeReason != null)
                false
            else if (expire == Constants.PUNISHMENT_NO_EXPIRE)
                true
            else if (expire == -1L)
                false
            else
                expire >= System.currentTimeMillis()
        }

    abstract fun errorMessage(): String
    abstract fun add(): Boolean
    abstract fun remove(): Boolean

    // just send the info that a punishment has been issued. Then retrieve it in the redis database from the id.
    override fun formatChannelMessage(): String {
        val json = JsonObject()
        json.addProperty("data-type", "punishment")
        json.addProperty("id", id)
        return json.toString()
    }

    companion object {
        var ID = 0

        fun subscribe() {
            val sub = RedisServer.newSubscriber(Constants.PUNISHMENT_CHANNEL)

            sub.parser { msg ->
                val jsonObject = JsonParser.parseString(msg).asJsonObject

                println(jsonObject)
            }
        }
    }
}