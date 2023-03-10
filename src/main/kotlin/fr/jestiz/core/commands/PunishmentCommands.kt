package fr.jestiz.core.commands

import fr.jestiz.core.Constants
import fr.jestiz.core.api.commands.Command
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.players.OfflineServerPlayer
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.punishments.Ban
import fr.jestiz.core.time.DurationParser
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class PunishmentCommands {

    @Command(names = ["ban"], permission = Constants.PERMISSION_BAN_COMMAND)
    fun banCommand(sender: CommandSender,
                   @Parameter(name = "target") offlineServerPlayer: OfflineServerPlayer,
                   @Parameter(name = "duration", required = false, default = "EVER") duration: DurationParser,
                   @Parameter(name = "reason", concat = true, required = false) reason: String?)
    {
        val serverPlayer = PlayerManager.getOnlinePlayer(if (sender is ConsoleCommandSender) Constants.CONSOLE_UUID else (sender as Player).uniqueId)
        val ban = Ban(serverPlayer.uuid, offlineServerPlayer.uuid)

        ban.duration = duration.millisTime
        if (reason != null) {
            ban.reason = reason
            ban.silent = reason.endsWith("-s")
        }
        
        ban.execute()
    }

}