package fr.jestiz.core.players

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.subscribers.UUIDLookupSubscriber
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.bukkit.Bukkit
import redis.clients.jedis.Jedis
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.TimeUnit

object PlayerManager {
    private val players = mutableMapOf<UUID, ServerPlayer>()
    private val offlinePlayers: ExpiringMap<UUID, OfflineServerPlayer> = ExpiringMap.builder()
        .expiration(Constants.OFFLINE_PLAYER_EXPIRE_TIME_MINS, TimeUnit.MINUTES) // TODO value to verify
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build()

    private val nameToUUID = mutableMapOf<String, UUID>()
    private val uuidToName = mutableMapOf<UUID, String>()

    /**
     * Gets a [ServerPlayer] instance
     *
     * @return The [ServerPlayer] corresponding to the uuid or a new instance if not found.
     */
    fun getOnlinePlayer(uuid: UUID): ServerPlayer {
        return players.getOrPut(uuid) { ServerPlayer(uuid) }
    }

    /**
     * Gets a [ServerPlayer] instance.
     * As it's by name, it's using [getUUID] which should be run async.
     *
     * @return The [ServerPlayer] corresponding to the uuid or a new instance if not found.
     */
    fun getOnlinePlayer(name: String): ServerPlayer? {
        val uuid = getUUID(name) ?: return null
        return players.getOrPut(uuid) { ServerPlayer(uuid) }
    }

    /**
     * Deletes a [ServerPlayer] instance.
     * It also adds an [OfflineServerPlayer] to an [ExpiringMap]
     * which expires [Constants.OFFLINE_PLAYER_EXPIRE_TIME_MINS] mins after the last access.
     *
     * @return The [ServerPlayer] corresponding to the uuid or a new instance if not found.
     */
    fun removeOnlinePlayer(uuid: UUID): ServerPlayer? {
        return players.remove(uuid)
    }

    /**
     * Check if the [ServerPlayer] exists
     */
    fun onlinePlayerExists(uuid: UUID): Boolean {
        return players.containsKey(uuid)
    }

    /**
     * Gets an [OfflineServerPlayer] instance
     *
     * @return The [OfflineServerPlayer] corresponding to the uuid or a new instance if not found.
     * If the player is online, a [ServerPlayer] will be returned.
     */
    fun getOfflinePlayer(uuid: UUID): OfflineServerPlayer {
        players[uuid]?.let {
            return it
        } ?: return offlinePlayers.getOrPut(uuid) { OfflineServerPlayer(uuid) }
    }

    /**
     * Gets a [ServerPlayer] instance.
     * As it's by name, it's using [getUUID] which should be run async.
     *
     * @return The [OfflineServerPlayer] corresponding to the uuid or a new instance if not found.
     * If the player is online, a [ServerPlayer] will be returned.
     */
    fun getOfflinePlayer(name: String): OfflineServerPlayer? {
        val uuid = getUUID(name) ?: return null
        players[uuid]?.let {
            return it
        } ?: return offlinePlayers.getOrPut(uuid) { OfflineServerPlayer(uuid) }
    }

    /**
    * Check if the [OfflineServerPlayer] exists
    */
    fun offlinePlayerExists(uuid: UUID): Boolean {
        return offlinePlayers.containsKey(uuid)
    }

    /**
     * Check if this server instance has a record of that UUID.
     * (making it have whether a [ServerPlayer] or [OfflineServerPlayer])
     *
     * @return true if any of [ServerPlayer] / [OfflineServerPlayer] was found.
     * false otherwise
     */
    fun hasRecordOf(uuid: UUID): Boolean {
        return offlinePlayers.containsKey(uuid) || onlinePlayerExists(uuid)
    }

    /**
     * Retrieves an [UUID] from a player name.
     * It first looks in the plugin's cache, then goes through redis
     * and finally if nothing was found it sends a message (through redis pub/sub)
     * to the database asking for the information.
     *
     * @return The [UUID] corresponding to the player name.
     */
    // Bukkit.getOfflinePlayer(String) is very slow (network I/O) hence this function.
    private fun getUUID(name: String): UUID? {
        // Fetches from plugin cache
        val lowName = name.lowercase(Locale.getDefault())
        val uuid = nameToUUID[lowName]
        uuid?.let { return uuid }

        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to retrieve an UUID in database from the main thread!")

        // Fetches from redis cache
        var uuidString: String? = null
        RedisServer.runCommand { uuidString = it.hget(Constants.REDIS_KEY_NAME_TO_UUID, lowName) }
        uuidString?.let { return UUID.fromString(uuidString) }

        // Fetches from database
        return UUIDLookupSubscriber.askForUUID(lowName).get()
    }

    fun updateUUIDCache(redis: Jedis, name: String, uuid: UUID) {
        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to update the UUID cache from the main thread!")

        val lowName = name.lowercase()

        nameToUUID[lowName] = uuid
        redis.hset(Constants.REDIS_KEY_NAME_TO_UUID, lowName, uuid.toString())

        uuidToName[uuid] = lowName
        redis.hset(Constants.REDIS_KEY_UUID_TO_NAME, uuid.toString(), lowName)
    }

    fun hasPlayedBefore(redis: Jedis, uuid: UUID): Boolean {
        // Fetches from plugin cache
        uuidToName[uuid]?.let { return true }

        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to retrieve an UUID in database from the main thread!")

        // Fetches from redis cache
        redis.hget(Constants.REDIS_KEY_UUID_TO_NAME, uuid.toString())?.let { return true }

        // TODO implement channel message to get from db
        return false
    }

    fun getOnlinePlayers(): List<ServerPlayer> {
        return players.values.toList()
    }

}