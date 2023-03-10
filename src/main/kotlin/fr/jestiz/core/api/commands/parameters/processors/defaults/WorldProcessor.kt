package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import java.util.*

class WorldProcessor : ParameterProcessor<World>() {

    override fun get(sender: CommandSender, source: String): World? {
        val world = Bukkit.getWorld(source)
        if (world == null)
            sender.sendMessage(Configurations.getConfigMessage("command.invalid-world", "%world%" to source))
        return world
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return Bukkit.getWorlds()
            .mapNotNull { it.name }
            .filter { it.lowercase(Locale.getDefault()).startsWith(source.lowercase(Locale.getDefault())) }
    }

}