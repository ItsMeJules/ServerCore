package fr.jestiz.core.database.redis.pubsub

interface RedisPublisher {
    fun formatChannelMessage(): String
}