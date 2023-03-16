package fr.jestiz.core.database.redis.pubsub

import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.RedisSettings
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

class RedisSubscriber(private val channel: String) {
    private lateinit var pubSub: JedisPubSub

    fun <T> parser(reader: (String) -> T) {
        pubSub = object : JedisPubSub() {
            override fun onMessage(channel: String, message: String) {
                try {
                    reader(message)
                } catch (e: Exception) {
                    System.err.println("An error occured when reading from channel: $channel")
                    e.printStackTrace()
                }
            }
        }
        subscribers[channel] = this
        Thread {
            RedisServer.runCommand { jedis -> jedis.subscribe(pubSub, channel) }
        }.start()
    }

    fun close() {
        pubSub.unsubscribe()
        subscribers.remove(channel)
    }

    companion object {
        val subscribers: MutableMap<String, RedisSubscriber> = HashMap(10)
    }
}