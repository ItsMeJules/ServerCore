package fr.jestiz.core.punishments

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.players.ServerPlayer
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import java.util.*

abstract class Punishment(protected val sender: UUID, protected val receiver: UUID): RedisPublisher {
    private var id = 0

    var reason: String = Configurations.getConfigMessage("punishment.ban.no-reason")
    var remover: UUID? = null
    var removeReason: String? = null
    val removed: Boolean
        get() = removeReason != null
    var silent = false
    var issued = System.currentTimeMillis()
    var expire = -1L
    var duration: Long
        get() {
            return if (expire == Long.MAX_VALUE)
                expire
            else
                expire - System.currentTimeMillis()
        }
        set(duration) {
            expire = if (duration != Long.MAX_VALUE)
                duration + System.currentTimeMillis()
            else
                duration
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

    open fun execute(reason: String): Boolean {
        val offlinePlayer = PlayerManager.getOfflinePlayer(receiver)

        if (this in offlinePlayer.punishments)
            return false

        val punishments = offlinePlayer.punishments

        this.reason = reason

        if (this is ServerRestrictedPunishment && offlinePlayer is ServerPlayer)
            kick(offlinePlayer, errorMessage())

        id = ID++
        punishments.add(this)
        RedisServer.publish(Constants.PUNISHMENT_CHANNEL, this)
        return true
    }


    open fun remove(remover: UUID, removeReason: String): Boolean {
        this.remover = remover
        this.removeReason = removeReason

        RedisServer.publish(Constants.PUNISHMENT_CHANNEL, this)
        return true
    }

    override fun formatChannelMessage(): String {
        val json = JsonObject()

        json.addProperty("data-type", "punishment")
        json.addProperty("id", id)
        json.addProperty("added", !removed)
        json.addProperty("silent", silent)
        json.addProperty("issued", issued)

        if (!removed) {
            json.addProperty("sender", sender.toString())
            json.addProperty("reason", reason)
            json.addProperty("expire", expire)
        } else {
            json.addProperty("sender", remover.toString())
            json.addProperty("reason", removeReason)
        }

        return json.toString()
    }

    companion object {
        var ID = 0

        fun subscribe() {
            val sub = RedisServer.newSubscriber(Constants.PUNISHMENT_CHANNEL)

            sub.parser { msg ->
                val jsonObject = JsonParser.parseString(msg).asJsonObject
            }
        }
    }
}