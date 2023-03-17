package fr.jestiz.core.database.redis.pubsub

import com.google.gson.JsonObject

interface RedisPublisher {
    fun formatChannelMessage(): JsonObject
}