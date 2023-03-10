package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import org.bukkit.command.CommandSender
import java.util.*

class BooleanProcessor : ParameterProcessor<Boolean>() {
    private val values = mapOf("true" to true, "on" to true, "yes" to true, "enable" to true, "oui" to true,
                                "false" to false, "off" to false, "no" to false, "disable" to false, "non" to false)

    override fun get(sender: CommandSender, source: String): Boolean? {
        var value = values[source.lowercase(Locale.getDefault())]

        if (value == null)
            sender.sendMessage(Configurations.getConfigMessage("command.value-invalid", "%val%" to source))

        return value
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return values.keys.filter { it.lowercase(Locale.getDefault()).startsWith(source.lowercase(Locale.getDefault())) }
    }
}