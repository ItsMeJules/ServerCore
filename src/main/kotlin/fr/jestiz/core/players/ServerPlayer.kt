package fr.jestiz.core.players

import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.RedisServer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.RuntimeException
import java.util.*


class ServerPlayer(uuid: UUID) : OfflineServerPlayer(uuid) {

    private val temporaryHiddenPlayers = listOf<UUID>()

    val player: Player
        get() = Bukkit.getPlayer(uuid)

    override fun load(): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to load a player from the main thread!")

        if (PlayerManager.offlinePlayerExists(uuid)) {
            transferInstance(PlayerManager.getOfflinePlayer(uuid))
        }

        RedisServer.runCommand { it.sadd(Constants.REDIS_CONNECTED_PLAYERS_LIST, uuid.toString()) }

        return super.load()
    }

    override fun onDisconnect(): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to save player info from the main thread!")

        // Saves the data of the ServerPlayer
        Bukkit.getScheduler().runTaskAsynchronously(Core.instance) {
            RedisServer.runCommand { it.srem(Constants.REDIS_CONNECTED_PLAYERS_LIST, uuid.toString()) }
            writeToRedis()
        }
        // Initializes a tmp OfflineServerPlayer
        PlayerManager.getOfflinePlayer(uuid).transferInstance(this)
        return super.onDisconnect()
    }

    override fun writeToRedis(): Boolean {
        super.writeToRedis()

        PlayerManager.updateUUIDCache(super.bukkitPlayer.name, uuid)

        return true
    }

}