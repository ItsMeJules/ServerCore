package fr.jestiz.core.database.redis

import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.pubsub.RedisPublisher
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
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
        runCommand { it.publish(channel, publisher.formatChannelMessage()) }
    }

    fun publish(channel: String, publisher: () -> String) {
        runCommand { it.publish(channel, publisher()) }
    }

    fun newSubscriber(channel: String): RedisSubscriber {
        return RedisSubscriber(settings, channel)
    }

    fun runCommand(run: (Jedis) -> Unit): Boolean {
        pool.resource.use { jedis ->
            try {
                run(jedis)
            } catch (e: Exception) {
                e.printStackTrace()
                pool.returnBrokenResource(jedis)
                false
            } finally {
                pool.returnResource(jedis)
            }
        }
        return true
    }
}