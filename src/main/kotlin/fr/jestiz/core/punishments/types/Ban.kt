package fr.jestiz.core.punishments.types

import fr.jestiz.core.Broadcaster
import fr.jestiz.core.Constants
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.fancymessage.FancyMessage
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.punishments.Punishment
import fr.jestiz.core.punishments.PunishmentType
import java.util.*

class Ban (sender: UUID, receiver: UUID): Punishment(sender, receiver, PunishmentType.BAN), ServerRestrictedPunishment {

    override fun errorMessage(): String {
        return Configurations.getConfigMessage("punishment.ban.kick-message",
            "%ban_date%" to Constants.DATE_FORMAT.format(issued),
            "%duration%" to if (duration == Long.MAX_VALUE) "jamais" else Constants.DATE_FORMAT.format(expire),
            "%reason%" to reason,
            "%id%" to id)
    }

    override fun notify(senderName: String, receiverName: String, removed: Boolean) {
        val staffMessageKey = "punishment.ban.staff-message.${if (removed) "removed" else "added"}-message"
        val silentPrefixKey = "punishment.ban.staff-message.silent-prefix"
        val playerMessageKey = "punishment.ban.player-message.${if (removed) "removed" else "added"}-message"
        val hoverKey = "punishment.ban.${if (removed) "hover-removed" else "hover-added"}"
    
        val staffMessage = Configurations.getConfigMessage(staffMessageKey,
            "%silent%" to if (silent) Configurations.getConfigMessage(silentPrefixKey) else "",
            "%receiver%" to receiverName,
            "%sender%" to senderName)
    
        Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustHave().broadCastNetwork(
            FancyMessage(staffMessage)
            .hoverEvent(FancyMessage.SHOW_TEXT)
            .hover(Configurations.getConfigMessage(hoverKey).replace("%reason%", reason))
        )
    
        if (!silent) {
            val playerMessage = Configurations.getConfigMessage(playerMessageKey,
                "%receiver%" to receiverName,
                "%sender%" to senderName)
    
            Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustNotHave().broadCastNetwork(
                FancyMessage(playerMessage)
                    .hoverEvent(FancyMessage.SHOW_TEXT)
                    .hover(Configurations.getConfigMessage(hoverKey).replace("%reason%", reason))
            )
        }
    }

}