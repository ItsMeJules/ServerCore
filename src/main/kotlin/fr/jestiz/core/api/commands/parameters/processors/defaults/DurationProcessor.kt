package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.time.DurationParser
import org.bukkit.command.CommandSender

class DurationProcessor : ParameterProcessor<DurationParser>() {

    private val defaults: List<String> = mutableListOf(
        "perm", "5m", "30m", "1h", "2h", "1d",
        "2d", "3d", "4d", "5d", "1w", "2w", "1M", "1y"
    )

    override fun get(sender: CommandSender, source: String): DurationParser? {
        val duration = DurationParser(source)
        if (duration.millisTime < 0) {
            sender.sendMessage(Configurations.getConfigMessage("command.duration-invalid", "%val%" to source))
            return null
        }
        return duration
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return defaults.filter { it.startsWith(source) }
    }

}