package fr.jestiz.core.database.redis

import com.google.gson.JsonObject
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import org.bukkit.Bukkit
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.io.FileNotFoundException

object RedisServer {
    private val pool: JedisPool
    private val settings: RedisSettings

    init {
        val config = Configurations.configs["redis.yml"] ?: throw FileNotFoundException("The file redis.yml is missing.")
        settings = RedisSettings(config.getString("password", ""),
                                    config.getString("address", "redis"),
                                    config.getInt("port", 6379))
        pool = JedisPool(settings.address, settings.port)

        if (settings.password.isNotEmpty())
            pool.resource.use { jedis -> jedis.auth(settings.password) }
    }

    val isActive: Boolean
        get() = !pool.isClosed

    fun publish(channel: String, publisher: RedisPublisher) {
        runCommand {
            val jsonObject = publisher.formatChannelMessage()

            jsonObject.addProperty("server-id", Bukkit.getServerId())
            it.publish(channel, jsonObject.toString())
        }
    }

    fun publish(channel: String, publisher: () -> JsonObject) {
        runCommand {
            val jsonObject = publisher()

            jsonObject.addProperty("server-id", Bukkit.getServerId())
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
        pool.close()
    }
}