package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.RedisServer
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.players.PlayerManager
import java.lang.RuntimeException
import java.util.*
import java.util.concurrent.CompletableFuture

object UUIDLookupSubscriber : RedisSubscriber(Constants.REDIS_UUID_LOOKUP_RESPONSE_CHANNEL) {

    private val completableFutures = mutableMapOf<String, CompletableFuture<UUID>>()

    override fun subscribe() {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject
            val name = jsonObject["name"]!!.asString // This can't be null

            completableFutures.remove(name)?.let {
                jsonObject["uuid"]?.let { jsonUuid ->

                    if (!jsonUuid.isJsonNull) {
                        val uuid = UUID.fromString(jsonUuid.asString)

                        RedisServer.runCommand { redis -> PlayerManager.updateUUIDCache(redis, name, uuid) }
                        it.complete(uuid)
                    } else
                        it.complete(null)

                } ?: it.complete(null)
            } ?: throw RuntimeException("Completable future $name not found when uuid lookup response was received!")
        }

        super.subscribe()
    }

    fun askForUUID(name: String): CompletableFuture<UUID> {
        val completableFuture = CompletableFuture<UUID>()
        completableFutures[name] = completableFuture

        RedisServer.publish(Constants.REDIS_UUID_LOOKUP_REQUEST_CHANNEL) { it.addProperty("name", name) }
        
        return completableFuture
    }

}