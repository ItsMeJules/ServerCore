package fr.jestiz.core.redis.pubsub

import fr.jestiz.core.redis.RedisSettings
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub

class RedisSubscriber(private val settings: RedisSettings, private val channel: String) {
    private lateinit var pubSub: JedisPubSub
    private val jedis: Jedis = Jedis(settings.address, settings.port).apply {
        if (settings.password.isNotEmpty())
            auth(settings.password)
    }

    fun <T> parser(reader: (String) -> T) {
        pubSub = object : JedisPubSub() {
            override fun onMessage(channel: String, message: String) {
                try {
                    println("Message received on channel: $channel")
                    reader(message)
                } catch (e: Exception) {
                    System.err.println("An error occured when reading from channel: $channel")
                    e.printStackTrace()
                }
            }
        }
        subscribers[channel] = this
        Thread { jedis.subscribe(pubSub, channel) }.start()
    }

    fun close() {
        pubSub.unsubscribe()
        jedis.close()
        subscribers.remove(channel)
    }

    companion object {
        private val subscribers: MutableMap<String, RedisSubscriber> = HashMap(10)

        fun getSubscriber(channel: String): RedisSubscriber? = subscribers[channel]
    }
}