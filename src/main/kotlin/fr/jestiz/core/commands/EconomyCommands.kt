package fr.jestiz.core.commands

import fr.jestiz.core.Constants
import fr.jestiz.core.api.commands.annotations.Command
import fr.jestiz.core.api.commands.annotations.SubCommand
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.players.OfflineServerPlayer
import fr.jestiz.core.players.ServerPlayer
import org.bukkit.command.CommandSender

class EconomyCommands {

    @Command(names = ["coins"], permission = Constants.PERMISSION_COINS_COMMAND)
    fun coinsCommand(sender: CommandSender) {

    }

    @SubCommand(parentCommand = "coins", name = "add", permission = Constants.PERMISSION_COINS_COMMAND, async = true)
    fun coinsAddCommand(sender: CommandSender,
                     @Parameter(name = "amount") amount: Int,
                     @Parameter(name = "target", required = false) offlineServerPlayer: OfflineServerPlayer?)
    {
    }

    @SubCommand(parentCommand = "coins", name = "remove server", permission = Constants.PERMISSION_COINS_COMMAND, async = true)
    fun coinsRemoveCommand(sender: CommandSender,
                        @Parameter(name = "amount") amount: Int,
                        @Parameter(name = "target", required = false) offlineServerPlayer: OfflineServerPlayer?)
    {
    }

    @SubCommand(parentCommand = "coins", name = "set", permission = Constants.PERMISSION_COINS_COMMAND, async = true)
    fun coinsSetCommand(sender: CommandSender,
                        @Parameter(name = "amount") amount: Int,
                        @Parameter(name = "target", required = false) offlineServerPlayer: OfflineServerPlayer?)
    {
    }
}