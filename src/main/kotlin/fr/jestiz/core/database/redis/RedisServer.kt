package fr.jestiz.core.database.redis

import com.google.gson.JsonObject
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.database.redis.subscribers.*
import fr.jestiz.core.players.OfflineServerPlayer
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.io.FileNotFoundException
import java.util.UUID

object RedisServer {
    private lateinit var pool: JedisPool
    private lateinit var settings: RedisSettings

    fun connect() {
        val config = Configurations.configs["redis.yml"] ?: throw FileNotFoundException("The file redis.yml is missing.")
        settings = RedisSettings(config.getString("password", ""),
            config.getString("address", "redis"),
            config.getInt("port", 6379))
        pool = JedisPool(settings.address, settings.port)

        if (settings.password.isNotEmpty())
            pool.resource.use { jedis -> jedis.auth(settings.password) }

        BroadcastSubscriber.subscribe()
        PlayerUpdateSubscriber.subscribe()
        PunishmentSubscriber.subscribe()
        UUIDLookupSubscriber.subscribe()
        PlayerMessageSubscriber.subscribe()
    }

    val isActive: Boolean
        get() = !pool.isClosed

    fun publish(channel: String, publisher: RedisPublisher) {
        publish(channel, publisher::formatChannelMessage)
    }

    fun publish(channel: String, publisher: (JsonObject) -> Unit) {
        runCommand {
            val jsonObject = JsonObject()
            jsonObject.addProperty("server-id", Core.serverID.toString())

            publisher(jsonObject)

            it.publish(channel, jsonObject.toString())
        }
    }

    fun newSubscriber(channel: String): RedisSubscriber {
        return RedisSubscriber(channel)
    }

    fun runCommand(run: (Jedis) -> Unit) {
        pool.resource.use { jedis -> run(jedis) }
    }

    fun setPlayerValue(uuid: UUID, field: String, value: String, publish: Boolean = true) {
        runCommand { it.set("$uuid:$field", value) }

        if (!publish)
            return

        publish(Constants.REDIS_PLAYER_UPDATE_CHANNEL) { jsonObject ->
            jsonObject.addProperty("uuid", uuid.toString())
            jsonObject.addProperty(field, value)
        }
    }

    fun closeConnections() {
        RedisSubscriber.subscribers.values.forEach { it.close() }
        RedisSubscriber.subscribers.clear()
        pool.close()
    }
}