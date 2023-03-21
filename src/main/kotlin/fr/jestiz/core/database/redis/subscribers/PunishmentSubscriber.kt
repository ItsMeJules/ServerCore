package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.punishments.Punishment
import org.bukkit.Bukkit

class PunishmentSubscriber : RedisSubscriber(Constants.REDIS_PUNISHMENT_CHANNEL) {

    init {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            if (jsonObject["server-id"]!!.asString != Bukkit.getServerId())
                Punishment.ID++
        }
    }

}