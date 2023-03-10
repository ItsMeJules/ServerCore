package fr.jestiz.core.punishments

import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.fancymessage.FancyMessage
import fr.jestiz.core.players.PlayerManager
import java.util.*

class Ban (sender: UUID, receiver: UUID): Punishment(sender, receiver), ServerRestrictedPunishment {

    override fun errorMessage(): String {
        return Configurations.getConfigMessage("punishment.ban.kick-message",
            "%ban_date%" to Constants.DATE_FORMAT.format(issued),
            "%reason%" to reason)
    }

    override fun execute(): Boolean {
        val added = super.execute()
        val offlinePlayer = PlayerManager.getOfflinePlayer(receiver)
        val senderName = if (Constants.CONSOLE_UUID == sender) "Console" else PlayerManager.getOfflinePlayer(sender).bukkitPlayer.name

        val staffMessage = Configurations.getConfigMessage("punishment.ban.staff-message.added-message",
            "%silent%" to if (silent) Configurations.getConfigMessage("punishment.ban.staff-message.silent-prefix") else "",
            "%receiver%" to offlinePlayer.bukkitPlayer.name,
            "%sender%" to senderName)

        val playerMessage = Configurations.getConfigMessage("punishment.ban.player-message.added-message",
            "%receiver%" to offlinePlayer.bukkitPlayer.name,
            "%sender%" to senderName)

        Core.broadcastWithPerm(FancyMessage(staffMessage)
            .hoverEvent(FancyMessage.SHOW_TEXT)
            .hover(Configurations.getConfigMessage("punishment.ban.staff-message.hover-added")), Constants.PERMISSION_BAN_COMMAND)
        if (!silent) {
            Core.broadcastWithoutPerm(FancyMessage(playerMessage)
                .hoverEvent(FancyMessage.SHOW_TEXT)
                .hover(Configurations.getConfigMessage("punishment.ban.player-message.hover-added")), Constants.PERMISSION_BAN_COMMAND)
        }
        return added
    }


    override fun remove(): Boolean {
        return true
    }

}