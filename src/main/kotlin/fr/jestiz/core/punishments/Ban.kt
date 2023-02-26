package fr.jestiz.core.punishments

import fr.jestiz.core.Constants
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.redis.RedisServer
import java.util.*

class Ban(private val sender: UUID, private val receiver: UUID): Punishment(sender, receiver) {

    override fun errorMessage(): String {
        return Configurations.configs["messages.yml"]?.let { config ->
            config.getString("punishment.ban.deny").replace("%ban_date%", Constants.DATE_FORMAT.format(issued))
                .replace("%reason%", reason ?: Constants.BAN_NO_REASON_FOUND)
        } ?: Constants.CONFIGURATION_NOT_FOUND
    }

    override fun add(): Boolean {
        RedisServer.publish(Constants.PUNISHMENT_CHANNEL, this)
        return true
    }

    override fun remove(): Boolean {
        return true
    }

}