package fr.jestiz.core.players

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.RedisServer
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.bukkit.Bukkit
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

object PlayerManager {
    private val players = mutableMapOf<UUID, ServerPlayer>()
    val offlinePlayers: ExpiringMap<UUID, OfflineServerPlayer> = ExpiringMap.builder()
        .expiration(Constants.OFFLINE_PLAYER_EXPIRE_TIME_MINS, TimeUnit.MINUTES) // TODO value to verify
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .build()

    private val nameToUUID = mutableMapOf<String, UUID>()
    private val completableFutures = mutableMapOf<String, CompletableFuture<UUID>>()

    init {
        RedisServer.newSubscriber("name-to-uuid").parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject
            val name = jsonObject["name"]!!.asString // This can't be null

            completableFutures.remove(name)?.let {
                jsonObject["uuid"]?.let { uuid ->
                    it.complete(UUID.fromString(uuid.asString))
                } ?: it.complete(null)
            }
        }
    }

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
     * Check if the [ServerPlayer] exists
     * As it's by name, it's using [getUUID] which should be run async.
     */
    fun onlinePlayerExists(name: String): Boolean {
        val uuid = getUUID(name) ?: return false
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
     * Gets a [ServerPlayer] instance
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
     * Check if the [OfflineServerPlayer] exists
     * As it's by name, it's using [getUUID] which should be run async.
     */
    fun offlinePlayerExists(name: String): Boolean {
        val uuid = getUUID(name) ?: return false
        return offlinePlayers.containsKey(uuid)
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
        var uuid = nameToUUID[name.lowercase(Locale.getDefault())]
        uuid?.let { return uuid }

        if (Bukkit.isPrimaryThread())
            throw RuntimeException("Trying to retrieve an UUID in database from the main thread!")

        // Fetches from redis cache
        var uuidString: String? = null
        RedisServer.runCommand { uuidString = it.hget("name-to-uuid", name.lowercase()) }
        uuidString?.let { return UUID.fromString(uuidString) }

        // Fetches from database
        val completableFuture = CompletableFuture<UUID>()
        completableFutures[name] = completableFuture

        RedisServer.publish("name-to-uuid") {
            val json = JsonObject()
            json.addProperty("name", name)
            return@publish json.toString()
        }

        return completableFuture.get()
    }

    fun updateUUIDCache(name: String, uuid: UUID) {
        nameToUUID[name.lowercase()] = uuid;
        RedisServer.runCommand { it.hset("name-to-uuid", name.lowercase(), uuid.toString()) }
    }

    fun getOnlinePlayers(): List<ServerPlayer> {
        return players.values.filterIsInstance<ServerPlayer>()
    }
}