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
        lateinit var permMessage: FancyMessage
        lateinit var noPermMessage: FancyMessage

        if (removed) {
            val staffMessage = Configurations.getConfigMessage("punishment.ban.staff-message.removed-message",
                "%silent%" to if (silent) Configurations.getConfigMessage("punishment.ban.staff-message.silent-prefix") else "",
                "%receiver%" to receiverName,
                "%sender%" to senderName)
            permMessage = FancyMessage(staffMessage)
                .hoverEvent(FancyMessage.SHOW_TEXT)
                .hover(Configurations.getConfigMessage("punishment.ban.staff-message.hover-removed").replace("%reason%", reason))

            if (!silent) {
                val playerMessage = Configurations.getConfigMessage("punishment.ban.player-message.removed-message",
                    "%receiver%" to receiverName,
                    "%sender%" to senderName)
                noPermMessage = FancyMessage(playerMessage)
                    .hoverEvent(FancyMessage.SHOW_TEXT)
                    .hover(Configurations.getConfigMessage("punishment.ban.player-message.hover-removed").replace("%reason%", reason))
                Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustNotHave().broadCastNetwork(noPermMessage)
            }
        } else {
            val staffMessage = Configurations.getConfigMessage("punishment.ban.staff-message.added-message",
                "%silent%" to if (silent) Configurations.getConfigMessage("punishment.ban.staff-message.silent-prefix") else "",
                "%receiver%" to receiverName,
                "%sender%" to senderName)
            permMessage = FancyMessage(staffMessage)
                .hoverEvent(FancyMessage.SHOW_TEXT)
                .hover(Configurations.getConfigMessage("punishment.ban.staff-message.hover-added").replace("%reason%", reason))
            if (!silent) {
                val playerMessage = Configurations.getConfigMessage("punishment.ban.player-message.added-message",
                    "%receiver%" to receiverName,
                    "%sender%" to senderName)
                noPermMessage = FancyMessage(playerMessage)
                    .hoverEvent(FancyMessage.SHOW_TEXT)
                    .hover(Configurations.getConfigMessage("punishment.ban.player-message.hover-added").replace("%reason%", reason))
            }
        }

        Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustHave().broadCastNetwork(permMessage)
        if (!silent)
            Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustNotHave().broadCastNetwork(noPermMessage)
    }

}