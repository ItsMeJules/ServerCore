package fr.jestiz.core.database

interface RedisWriter {

    fun writeToRedis(): Boolean

}