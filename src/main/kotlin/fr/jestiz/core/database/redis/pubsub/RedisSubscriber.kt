package fr.jestiz.core.database.redis.pubsub

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisSettings
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashMap
import kotlin.concurrent.thread

open class RedisSubscriber(private val channel: String) {
    private lateinit var pubSub: JedisPubSub

    fun <T> parser(reader: (String) -> T) {
        pubSub = object : JedisPubSub() {
            override fun onMessage(channel: String, message: String) {
                try {
                    reader(message)
                } catch (e: Exception) {
                    System.err.println("An error occurred when reading from channel: $channel")
                    e.printStackTrace()
                }
            }
        }
        subscribers[channel] = this
        thread(start = true, isDaemon = true, name = "subscriber_$channel") {
            RedisServer.runCommand { jedis -> jedis.subscribe(pubSub, channel) }
        }
    }

    fun isServerSender(jsonObject: JsonObject): Boolean {
        jsonObject["server-id"]?.let {
            println(UUID.fromString(it.asString) == Core.serverID)
            return UUID.fromString(it.asString) == Core.serverID } ?: return false
    }

    fun close() {
        pubSub.unsubscribe()
        subscribers.remove(channel)
    }

    open fun subscribe() {
        Bukkit.getLogger().info("subscribed to channel $channel")
    }

    companion object {
        val subscribers: MutableMap<String, RedisSubscriber> = HashMap(10)

        fun register(subscriber: RedisSubscriber) {
            subscriber.subscribe()
        }
    }
}