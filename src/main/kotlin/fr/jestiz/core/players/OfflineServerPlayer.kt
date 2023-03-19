package fr.jestiz.core.players

import com.google.gson.JsonObject
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisWriter
import fr.jestiz.core.punishments.Punishment
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
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

    fun isOnline(): Boolean { // A changer pour verifier sur tous les serveurs
        return bukkitPlayer.isOnline
    }

    override fun writeToRedis(): Boolean {


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