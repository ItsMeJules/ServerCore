package fr.jestiz.core.commands

import fr.jestiz.core.Constants
import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.annotations.Command
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.players.OfflineServerPlayer
import fr.jestiz.core.punishments.Ban
import fr.jestiz.core.time.DurationParser
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

class PunishmentCommands {

    @Command(names = ["ban"], permission = Constants.PERMISSION_BAN_COMMAND, async = true)
    fun banCommand(sender: CommandSender,
                   @Parameter(name = "target") offlineServerPlayer: OfflineServerPlayer,
                   @Parameter(name = "duration", default = "EVER") duration: DurationParser,
                   @Parameter(name = "reason", concat = true) reason: String)
    {
        val ban = Ban(if (sender is ConsoleCommandSender) Constants.CONSOLE_UUID else (sender as Player).uniqueId, offlineServerPlayer.uuid)

        ban.duration = duration.millisTime
        ban.silent = reason.endsWith("-s")

        Bukkit.getScheduler().runTask(Core.instance) {
            ban.execute(if (ban.silent) reason.substring(0, reason.length - 2) else reason)
        }
    }

}