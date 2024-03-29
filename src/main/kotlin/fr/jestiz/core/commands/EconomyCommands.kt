package fr.jestiz.core.commands

import fr.jestiz.core.Constants
import fr.jestiz.core.api.commands.annotations.Command
import fr.jestiz.core.api.commands.annotations.SubCommand
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.database.redis.RedisServer
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
                     @Parameter(name = "target") offlineServerPlayer: OfflineServerPlayer)
    {
        offlineServerPlayer.coins += amount

        sender.sendMessage(Configurations.getConfigMessage("command.coins.added-to-player",
            "%coins%" to amount,
            "%receiver%" to offlineServerPlayer.bukkitPlayer.name))
        offlineServerPlayer.ifOnNetwork {
            offlineServerPlayer.sendNetworkMessage(Configurations.getConfigMessage("command.coins.added-by-player",
                "%coins%" to amount,
                "%sender%" to sender.name))
        }

        RedisServer.setPlayerValue(offlineServerPlayer.uuid, Constants.REDIS_KEY_PLAYER_COINS, offlineServerPlayer.coins.toString())
    }

    @SubCommand(parentCommand = "coins", name = "remove server", permission = Constants.PERMISSION_COINS_COMMAND, async = true)
    fun coinsRemoveCommand(sender: CommandSender,
                        @Parameter(name = "amount") amount: Int,
                        @Parameter(name = "target") offlineServerPlayer: OfflineServerPlayer)
    {
    }

    @SubCommand(parentCommand = "coins", name = "set", permission = Constants.PERMISSION_COINS_COMMAND, async = true)
    fun coinsSetCommand(sender: CommandSender,
                        @Parameter(name = "amount") amount: Int,
                        @Parameter(name = "target") offlineServerPlayer: OfflineServerPlayer)
    {
    }

}