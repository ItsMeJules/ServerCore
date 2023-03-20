package fr.jestiz.core.database.redis

import redis.clients.jedis.Jedis

interface RedisWriter {

    fun writeToRedis(redis: Jedis): Boolean

}