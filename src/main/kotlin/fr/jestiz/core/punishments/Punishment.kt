package fr.jestiz.core.punishments

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisWriter
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.players.ServerPlayer
import org.bukkit.Bukkit
import java.lang.RuntimeException
import java.util.*

abstract class Punishment(protected val sender: UUID, protected val receiver: UUID): RedisPublisher, RedisWriter {
    protected var id = 0

    var reason: String = Configurations.getConfigMessage("punishment.ban.no-reason")
    private var remover: UUID? = null
    private var removeReason: String? = null
    private val removed: Boolean
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

    /**
     * Executes the punishment, kicks the player if the class
     * implements [ServerRestrictedPunishment].
     * It writes it to the redis database and publishes a message.
     *
     * @return false if the player already has this punishment.
     * true if not.
     */
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
        writeToRedis()
        RedisServer.publish(Constants.PUNISHMENT_CHANNEL, this)
        return true
    }


    open fun remove(remover: UUID, removeReason: String): Boolean {
        this.remover = remover
        this.removeReason = removeReason

        RedisServer.publish(Constants.PUNISHMENT_CHANNEL, this)
        return true
    }

    override fun formatChannelMessage(): JsonObject {
        val json = JsonObject()

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

        return json
    }

    override fun writeToRedis(): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to save a punishment from the main thread!")

        RedisServer.runCommand { redis ->
            redis.hmset(
                "punishment:$id",
                mapOf("sender" to sender.toString(),
                        "receiver" to receiver.toString(),
                        "reason" to reason,
                        "remover" to if (remover == null) "" else remover.toString(),
                        "remove_reason" to if (removeReason == null) "" else removeReason,
                        "silent" to silent.toString(),
                        "issued" to issued.toString(),
                        "expire" to expire.toString())
            )
        }

        return true
    }

    companion object {
        private var ID = 0

        init { // No need for async as it's at startup
            RedisServer.runCommand { redis -> ID = redis.get(Constants.REDIS_PUNISHMENTS_LAST_ID_KEY).toInt() }
        }

        fun subscribe() {
            val sub = RedisServer.newSubscriber(Constants.PUNISHMENT_CHANNEL)

            sub.parser { msg ->
                val jsonObject = JsonParser.parseString(msg).asJsonObject

                if (jsonObject["server-id"]!!.asString != Bukkit.getServerId())
                    ID++
            }
        }
    }
}