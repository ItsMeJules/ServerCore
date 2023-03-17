package fr.jestiz.core.database.redis

interface RedisWriter {

    fun writeToRedis(): Boolean

}