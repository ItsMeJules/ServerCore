package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import org.bukkit.command.CommandSender

class LongProcessor : ParameterProcessor<Long>() {

    override fun get(sender: CommandSender, source: String): Long? {
        return try {
            source.toLong()
        } catch (ex: Exception) {
            sender.sendMessage(Configurations.getConfigMessage("command.value-invalid", "%val%" to source))
            return null
        }
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return mutableListOf()
    }

}