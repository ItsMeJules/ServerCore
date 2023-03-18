package fr.jestiz.core.commands

import fr.jestiz.core.Constants
import fr.jestiz.core.api.commands.annotations.Command
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.ServerPlayer
import org.bukkit.GameMode

class EssentialsCommands {

    @Command(names = ["gamemode", "gm"], permission = Constants.PERMISSION_GAMEMODE_COMMAND, playerOnly = true)
    fun gamemodeCommand(serverPlayer: ServerPlayer,
                        @Parameter(name = "gamemode") gamemode: GameMode,
                        @Parameter(name = "target", required = false) targetServerPlayer: ServerPlayer?)
    {
        if (targetServerPlayer != null) {
            targetServerPlayer.player.gameMode = gamemode
            targetServerPlayer.player.updateInventory()
            targetServerPlayer.player.sendMessage(Configurations.getConfigMessage("command.gamemode.updated-by-player",
                "%gamemode%" to gamemode.name,
                            "%sender%" to serverPlayer.player.name))
            serverPlayer.player.sendMessage(Configurations.getConfigMessage("command.gamemode.updated-to-player",
                "%gamemode%" to gamemode.name,
                "%receiver%" to targetServerPlayer.player.name))
        } else {
            serverPlayer.player.gameMode = gamemode
            serverPlayer.player.updateInventory()
            serverPlayer.player.sendMessage(Configurations.getConfigMessage("command.gamemode.updated",
                "%gamemode%" to gamemode.name))
        }
    }

}