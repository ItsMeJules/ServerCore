package fr.jestiz.core.punishments

import com.google.gson.JsonObject
import fr.jestiz.core.Constants
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisWriter
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.players.ServerPlayer
import fr.jestiz.core.punishments.types.Ban
import fr.jestiz.core.punishments.types.ServerRestrictedPunishment
import org.bukkit.Bukkit
import redis.clients.jedis.Jedis
import java.lang.RuntimeException
import java.util.*

abstract class Punishment(protected val sender: UUID, protected val receiver: UUID, private val type: PunishmentType)
    : RedisPublisher, RedisWriter {

    var id = 0

    var reason: String = Configurations.getConfigMessage("punishment.ban.no-reason")
    var remover: UUID? = null
    var removeReason: String? = null
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
    abstract fun notify(senderName: String, receiverName: String)

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

        if (this is ServerRestrictedPunishment && offlinePlayer is ServerPlayer)
            kick(offlinePlayer, errorMessage())

        this.reason = reason
        this.id = ID++
        offlinePlayer.punishments.add(this)
        RedisServer.publish(Constants.REDIS_PUNISHMENT_CHANNEL, this)
        return true
    }


    open fun remove(remover: UUID, removeReason: String): Boolean {
        this.remover = remover
        this.removeReason = removeReason

        RedisServer.publish(Constants.REDIS_PUNISHMENT_CHANNEL, this)
        return true
    }

    override fun formatChannelMessage(): JsonObject {
        val json = JsonObject()

        json.addProperty("id", id)
        json.addProperty("receiver", receiver.toString())
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

    override fun writeToRedis(redis: Jedis): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to save a punishment from the main thread!")

        redis.rpush("$receiver:${Constants.REDIS_KEY_PLAYER_PUNISHMENTS_IDS}", id.toString())
        redis.hset("$receiver:${Constants.REDIS_KEY_PLAYER_PUNISHMENTS}:$id",
            mapOf("type" to type.name,
                "sender" to sender.toString(),
                "reason" to reason,
                "remover" to if (remover == null) "" else remover.toString(),
                "remove_reason" to if (removeReason == null) "" else removeReason,
                "silent" to silent.toString(),
                "issued" to issued.toString(),
                "expire" to expire.toString())
        )

        return true
    }

    companion object {
        var ID = 0

        init { // No need for async as it's at startup
            RedisServer.runCommand { redis -> redis.get(Constants.REDIS_KEY_PUNISHMENTS_LAST_ID)?.let { ID = it.toInt() } }
        }

        fun from(type: PunishmentType, sender: UUID, receiver: UUID): Punishment {
            when (type) {
                PunishmentType.BAN -> return Ban(sender, receiver)
                else -> throw RuntimeException("Punishment type ${type.name} not handled!")
            }
        }

        fun saveIDs() {
            RedisServer.runCommand { redis -> redis.set(Constants.REDIS_KEY_PUNISHMENTS_LAST_ID, ID.toString()) }
        }
    }
}