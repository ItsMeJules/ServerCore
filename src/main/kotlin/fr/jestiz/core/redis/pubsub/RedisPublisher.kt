package fr.jestiz.core.redis.pubsub

interface RedisPublisher {
    fun formatChannelMessage(): String
}