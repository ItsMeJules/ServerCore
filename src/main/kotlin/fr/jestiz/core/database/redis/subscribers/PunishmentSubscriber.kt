package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.punishments.Punishment
import org.bukkit.Bukkit
import java.util.*

object PunishmentSubscriber : RedisSubscriber(Constants.REDIS_PUNISHMENT_CHANNEL) {

    override fun subscribe() {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            if (UUID.fromString(jsonObject["server-id"]!!.asString) != Core.serverID)
                Punishment.ID++
        }
        super.subscribe()
    }

}