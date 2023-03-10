package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.OfflineServerPlayer
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*

class OfflineServerPlayerProcessor : ParameterProcessor<OfflineServerPlayer>() {

    override fun get(sender: CommandSender, source: String): OfflineServerPlayer? {
        val player = PlayerManager.getOfflinePlayer(source)
        if (player == null)
            sender.sendMessage(Configurations.getConfigMessage("command.player-not-found", "%player%" to source))
        return player
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return Bukkit.getOfflinePlayers()
            .mapNotNull { it.name }
            .filter { it.lowercase(Locale.getDefault()).startsWith(source.lowercase(Locale.getDefault())) }
            .take(100)
    }
}