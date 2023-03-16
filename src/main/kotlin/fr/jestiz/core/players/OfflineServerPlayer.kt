package fr.jestiz.core.players

import fr.jestiz.core.database.RedisWriter
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

    fun <T : Punishment> getPunishments(kClass: KClass<T>)
        = punishments.filterIsInstance(kClass.java).map { it }

    open fun load(): Boolean {
        loaded = true
        return true
    }

    override fun writeToRedis(): Boolean {
        return true
    }

    open fun transferInstance(offlineServerPlayer: OfflineServerPlayer) {
        punishments.addAll(offlineServerPlayer.punishments)
    }
}