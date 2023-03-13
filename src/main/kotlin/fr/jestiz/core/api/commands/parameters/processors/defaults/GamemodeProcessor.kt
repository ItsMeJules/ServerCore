package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import java.util.*

class GamemodeProcessor : ParameterProcessor<GameMode>() {

    private val defaults = mapOf("creative" to GameMode.CREATIVE, "c" to GameMode.CREATIVE, "1" to GameMode.CREATIVE,
                                "survival" to GameMode.SURVIVAL, "s" to GameMode.SURVIVAL, "0" to GameMode.SURVIVAL,
                                "adventure" to GameMode.ADVENTURE, "a" to GameMode.ADVENTURE, "2" to GameMode.ADVENTURE,
                                "spectator" to GameMode.SPECTATOR, "sp" to GameMode.SPECTATOR, "3" to GameMode.SPECTATOR,
    )

    override fun get(sender: CommandSender, source: String): GameMode? {
        val gameMode = defaults[source.lowercase(Locale.getDefault())]
        if (gameMode == null)
            sender.sendMessage(Configurations.getConfigMessage("command.gamemode.not-found", "%val%" to source))

        return gameMode
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return GameMode.values().map { it.name.lowercase(Locale.getDefault()) }.filter {
            println("$it | $source ||| ${it.startsWith(source.lowercase(Locale.getDefault()))}")
            it.startsWith(source.lowercase(Locale.getDefault()))
        }
    }
}