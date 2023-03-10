package fr.jestiz.core.punishments

import fr.jestiz.core.players.ServerPlayer

interface ServerRestrictedPunishment {

    fun kick(serverPlayer: ServerPlayer, msg: String) {
        serverPlayer.player.kickPlayer(msg)
    }

}