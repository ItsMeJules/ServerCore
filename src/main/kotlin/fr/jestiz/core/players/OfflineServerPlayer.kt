package fr.jestiz.core.players

import com.google.gson.JsonObject
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisWriter
import fr.jestiz.core.punishments.Punishment
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import redis.clients.jedis.Jedis
import java.util.*
import kotlin.reflect.KClass

open class OfflineServerPlayer(val uuid: UUID): RedisWriter {

    lateinit var address: String

    var loaded = false

    val bukkitPlayer: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(uuid)
    val punishments = mutableListOf<Punishment>()

    var coins = 0

    fun <T : Punishment> getPunishments(kClass: KClass<T>)
        = punishments.filterIsInstance(kClass.java).map { it }

    open fun load(): Boolean {
        loaded = true
        return true
    }

    fun ifOnline(callback: (ServerPlayer) -> Unit): Unit {
        Bukkit.getScheduler().runTaskAsynchronously(Core.instance) {
            RedisServer.runCommand {
                if (it.sismember(Constants.REDIS_KEY_CONNECTED_PLAYERS_LIST, uuid.toString()))
                    callback(this as ServerPlayer)
            }
        }
    }

    // TODO only write data that has changed to redis. (could implement writing queue)
    override fun writeToRedis(redis: Jedis): Boolean {
        redis.set("$uuid:${Constants.REDIS_KEY_PLAYER_DATA_HSET_COINS}", coins.toString())
        punishments.forEach { it.writeToRedis(redis) }

        RedisServer.publish(Constants.REDIS_PLAYER_UPDATE_CHANNEL) {
            val jsonObject = JsonObject()

            jsonObject.addProperty("uuid", uuid.toString())

            return@publish jsonObject
        }
        return true
    }

    open fun transferInstance(offlineServerPlayer: OfflineServerPlayer) {
        punishments.addAll(offlineServerPlayer.punishments)
    }
}