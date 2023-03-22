package fr.jestiz.core.players

import com.google.gson.JsonObject
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisWriter
import fr.jestiz.core.punishments.Punishment
import fr.jestiz.core.punishments.PunishmentType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import redis.clients.jedis.Jedis
import java.util.*
import kotlin.reflect.KClass

open class OfflineServerPlayer(val uuid: UUID) {

    val bukkitPlayer: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(uuid)

    var loaded = false
    val punishments = mutableListOf<Punishment>() // no need to save to redis onDisconnect()
    var coins = 0
        set (value) {
            field = value
            RedisServer.runCommand { it.set("$uuid:${Constants.REDIS_KEY_PLAYER_COINS}", coins.toString()) }
            RedisServer.publish(Constants.REDIS_PLAYER_UPDATE_CHANNEL) { jsonObject ->
                jsonObject.addProperty("uuid", uuid.toString())
                jsonObject.addProperty("coins", value)
            }
        }

    fun <T : Punishment> getPunishments(kClass: KClass<T>)
        = punishments.filterIsInstance(kClass.java).map { it }

    fun getPunishmentById(id: Int)
        = punishments.firstOrNull { it.id == id }

    open fun load(redis: Jedis): Boolean {
        coins = redis.get("$uuid:${Constants.REDIS_KEY_PLAYER_COINS}").toInt()

        // None of the keys in here can be null
        for (idStr in redis.lrange("$uuid:${Constants.REDIS_KEY_PLAYER_PUNISHMENTS_IDS}", 0, -1)) {
            val punishmentRedis = redis.hgetAll("$uuid:${Constants.REDIS_KEY_PLAYER_PUNISHMENTS}:$idStr")
            val punishmentType = PunishmentType.valueOf(punishmentRedis["type"]!!)
            val punishment = Punishment.from(punishmentType, UUID.fromString(punishmentRedis["sender"]!!), uuid)

            punishment.reason = punishmentRedis["reason"]!!
            punishmentRedis["remover"]?.let { if (it.isNotEmpty()) punishment.remover = UUID.fromString(it) }
            punishmentRedis["remove_reason"]?.let { if (it.isNotEmpty()) punishment.removeReason = it }
            punishment.silent = punishmentRedis["silent"]!!.toBoolean()
            punishment.issued = punishmentRedis["issued"]!!.toLong()
            punishment.expire = punishmentRedis["expire"]!!.toLong()
        }

        loaded = true
        return true
    }

    fun ifOnline(callback: (ServerPlayer) -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.instance) {
            RedisServer.runCommand {
                if (it.sismember(Constants.REDIS_KEY_CONNECTED_PLAYERS_LIST, uuid.toString()))
                    callback(this as ServerPlayer)
            }
        }
    }

    open fun transferInstance(offlineServerPlayer: OfflineServerPlayer) {
        punishments.addAll(offlineServerPlayer.punishments)
        coins = offlineServerPlayer.coins
    }
}