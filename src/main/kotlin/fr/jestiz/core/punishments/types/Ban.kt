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

    override fun notify(senderName: String, receiverName: String) {
        val staffMessage = Configurations.getConfigMessage("punishment.ban.staff-message.added-message",
            "%silent%" to if (silent) Configurations.getConfigMessage("punishment.ban.staff-message.silent-prefix") else "",
            "%receiver%" to receiverName,
            "%sender%" to senderName)
        val permMessage = FancyMessage(staffMessage)
            .hoverEvent(FancyMessage.SHOW_TEXT)
            .hover(Configurations.getConfigMessage("punishment.ban.staff-message.hover-added").replace("%reason%", reason))

        Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustHave().broadCastNetwork(permMessage)

        if (!silent) {
            val playerMessage = Configurations.getConfigMessage("punishment.ban.player-message.added-message",
                "%receiver%" to receiverName,
                "%sender%" to senderName)
            val noPermMessage = FancyMessage(playerMessage)
                .hoverEvent(FancyMessage.SHOW_TEXT)
                .hover(Configurations.getConfigMessage("punishment.ban.player-message.hover-added").replace("%reason%", reason))

            Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustNotHave().broadCastNetwork(noPermMessage)
        }
    }

    override fun execute(reason: String): Boolean {
        val added = super.execute(reason)
        val senderName = if (Constants.CONSOLE_UUID == sender) "Console" else PlayerManager.getOfflinePlayer(sender).bukkitPlayer.name

        notify(senderName, PlayerManager.getOfflinePlayer(receiver).bukkitPlayer.name)

        return added
    }


    override fun remove(remover: UUID, removeReason: String): Boolean {
        val removed = super.remove(remover, removeReason)
        val offlinePlayer = PlayerManager.getOfflinePlayer(receiver)
        val senderName = if (Constants.CONSOLE_UUID == remover) "Console" else PlayerManager.getOfflinePlayer(remover).bukkitPlayer.name

        val staffMessage = Configurations.getConfigMessage("punishment.ban.staff-message.removed-message",
            "%silent%" to if (silent) Configurations.getConfigMessage("punishment.ban.staff-message.silent-prefix") else "",
            "%receiver%" to offlinePlayer.bukkitPlayer.name,
            "%sender%" to senderName)
        val permMessage = FancyMessage(staffMessage)
            .hoverEvent(FancyMessage.SHOW_TEXT)
            .hover(Configurations.getConfigMessage("punishment.ban.staff-message.hover-removed").replace("%reason%", reason))
        Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustHave().broadCastNetwork(permMessage)


        if (!silent) {
            val playerMessage = Configurations.getConfigMessage("punishment.ban.player-message.removed-message",
                "%receiver%" to offlinePlayer.bukkitPlayer.name,
                "%sender%" to senderName)
            val noPermMessage = FancyMessage(playerMessage)
                .hoverEvent(FancyMessage.SHOW_TEXT)
                .hover(Configurations.getConfigMessage("punishment.ban.player-message.hover-removed").replace("%reason%", reason))
            Broadcaster().viewPermission(Constants.PERMISSION_BAN_COMMAND).mustNotHave().broadCastNetwork(noPermMessage)
        }

        return removed
    }

}