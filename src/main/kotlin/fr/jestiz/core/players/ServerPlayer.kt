package fr.jestiz.core.players

import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.RedisServer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


class ServerPlayer(uuid: UUID) : OfflineServerPlayer(uuid) {

    private val temporaryHiddenPlayers = listOf<UUID>()

    val player: Player
        get() = Bukkit.getPlayer(uuid)

    override fun load(): Boolean {
        if (PlayerManager.offlinePlayerExists(uuid)) {
            transferInstance(PlayerManager.getOfflinePlayer(uuid))
        }

        RedisServer.runCommand { it.rpush(Constants.REDIS_CONNECTED_PLAYERS_LIST, uuid.toString()) }

        return super.load()
    }

    override fun onDisconnect(): Boolean {
        // Saves the data of the ServerPlayer
        Bukkit.getScheduler().runTaskAsynchronously(Core.instance) { writeToRedis() }
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