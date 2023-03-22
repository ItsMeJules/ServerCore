package fr.jestiz.core.database.redis

import com.google.gson.JsonObject
import fr.jestiz.core.Core
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.database.redis.subscribers.BroadcastSubscriber
import fr.jestiz.core.database.redis.subscribers.PlayerUpdateSubscriber
import fr.jestiz.core.database.redis.subscribers.PunishmentSubscriber
import fr.jestiz.core.database.redis.subscribers.UUIDLookupSubscriber
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.io.FileNotFoundException

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

    fun runCommand(run: (Jedis) -> Unit): Boolean {
        pool.resource.use { jedis -> run(jedis) }
        return true
    }

    fun closeConnections() {
        RedisSubscriber.subscribers.values.forEach { it.close() }
        RedisSubscriber.subscribers.clear()
        pool.close()
    }
}