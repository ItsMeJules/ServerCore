package fr.jestiz.core.players

import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.RedisServer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import redis.clients.jedis.Jedis
import java.lang.RuntimeException
import java.util.*


class ServerPlayer(uuid: UUID) : OfflineServerPlayer(uuid) {

    private val temporaryHiddenPlayers = listOf<UUID>()

    val player: Player
        get() = Bukkit.getPlayer(uuid)

    override fun load(redis: Jedis): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to load a player from the main thread!")

        var loadSuccess = true

        // As each instance has the last updated player version,
        // I don't need to query the redis server when the player connects.
        if (PlayerManager.offlinePlayerExists(uuid)) {
            transferInstance(PlayerManager.getOfflinePlayer(uuid))
        } else if (PlayerManager.hasPlayedBefore(redis, uuid)) {
            loadSuccess = super.load(redis)
        }

        redis.sadd(Constants.REDIS_KEY_CONNECTED_PLAYERS_LIST, uuid.toString())
        return loadSuccess
    }

    fun onJoin(redis: Jedis, event: AsyncPlayerPreLoginEvent): Boolean {
        if (!loaded && !load(redis)) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = Configurations.getConfigMessage("error.player-load")
        }

        PlayerManager.updateUUIDCache(redis, event.name, event.uniqueId)
        return true
    }

    fun onDisconnect(redis: Jedis): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to save a player info from the main thread!")

        // Saves the data of the ServerPlayer
        redis.srem(Constants.REDIS_KEY_CONNECTED_PLAYERS_LIST, uuid.toString())
        PlayerManager.updateUUIDCache(redis, super.bukkitPlayer.name, uuid)

        // Initializes a tmp OfflineServerPlayer
        PlayerManager.getOfflinePlayer(uuid).transferInstance(this)
        return true
    }

}