package fr.jestiz.core.punishments

import fr.jestiz.core.Core
import fr.jestiz.core.players.ServerPlayer
import org.bukkit.Bukkit

interface ServerRestrictedPunishment {

    fun kick(serverPlayer: ServerPlayer, msg: String) {
        if (!Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTask(Core.instance) { serverPlayer.player.kickPlayer(msg) }
        else
            serverPlayer.player.kickPlayer(msg)
    }

}