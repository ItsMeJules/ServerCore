package fr.jestiz.core.api.commands.parameters.processors.defaults

import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.PlayerManager
import fr.jestiz.core.players.ServerPlayer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*

class ServerPlayerProcessor : ParameterProcessor<ServerPlayer>() {

    override fun get(sender: CommandSender, source: String): ServerPlayer? {
//        if (sender is Player && source == "")
//            return sender

        val player = PlayerManager.getOnlinePlayer(source)
        if (player == null)
            sender.sendMessage(Configurations.getConfigMessage("command.player-not-found", "%player%" to source))
        return player
    }

    override fun tabComplete(sender: CommandSender, source: String): List<String> {
        return Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.lowercase(Locale.getDefault()).startsWith(source.lowercase(Locale.getDefault())) }
    }

}