package fr.jestiz.core.database.redis.subscribers

import com.google.gson.JsonParser
import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.database.redis.pubsub.RedisSubscriber
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.punishments.Punishment
import fr.jestiz.core.punishments.PunishmentType
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object PunishmentSubscriber : RedisSubscriber(Constants.REDIS_PUNISHMENT_CHANNEL) {

    override fun subscribe() {
        parser { msg ->
            val jsonObject = JsonParser.parseString(msg).asJsonObject

            if (isServerSender(jsonObject))
                return@parser

            val receiver = UUID.fromString(jsonObject["receiver"]!!.asString)
            if (!PlayerManager.hasRecordOf(receiver))
                return@parser

            val reason = jsonObject["reason"]!!.asString
            val sender = UUID.fromString(jsonObject["sender"]!!.asString)
            val added = jsonObject["added"]!!.asBoolean
            lateinit var punishment: Punishment

            if (added) {
                punishment = Punishment.from(PunishmentType.valueOf(jsonObject["type"]!!.asString), sender, receiver)
                punishment.expire = jsonObject["expire"]!!.asLong
            } else
                PlayerManager.getOfflinePlayer(receiver).getPunishmentById(jsonObject["id"]!!.asInt)?.let { punishment = it }

            punishment.silent = jsonObject["silent"]!!.asBoolean
            punishment.issued = jsonObject["issued"]!!.asLong

            if (added)
                punishment.execute(reason)
            else
                punishment.remove(sender, reason)
        }
        super.subscribe()
    }

}