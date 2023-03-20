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

    override fun load(): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to load a player from the main thread!")

        if (PlayerManager.offlinePlayerExists(uuid)) {
            transferInstance(PlayerManager.getOfflinePlayer(uuid))
        }

        RedisServer.runCommand { it.sadd(Constants.REDIS_KEY_CONNECTED_PLAYERS_LIST, uuid.toString()) }

        return super.load()
    }

    fun onJoin(event: AsyncPlayerPreLoginEvent): Boolean {
        if (!loaded && !load()) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = Configurations.getConfigMessage("error.player-load")
        }

        RedisServer.runCommand { PlayerManager.updateUUIDCache(it, event.name, event.uniqueId) }
        return true
    }

    fun onDisconnect(): Boolean {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to save a player info from the main thread!")

        // Saves the data of the ServerPlayer
        RedisServer.runCommand {
            it.srem(Constants.REDIS_KEY_CONNECTED_PLAYERS_LIST, uuid.toString())
            writeToRedis(it)
        }

        // Initializes a tmp OfflineServerPlayer
        PlayerManager.getOfflinePlayer(uuid).transferInstance(this)
        return true
    }

    override fun writeToRedis(redis: Jedis): Boolean {
        super.writeToRedis(redis)

        PlayerManager.updateUUIDCache(redis, super.bukkitPlayer.name, uuid)

        return true
    }

}