package fr.jestiz.core.listeners

import fr.jestiz.core.Core
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class ServerPlayerListener : Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    fun onPreLoginSetup(event: AsyncPlayerPreLoginEvent) {
        val serverPlayer = PlayerManager.getOnlinePlayer(event.uniqueId)

        if (!serverPlayer.loaded && !serverPlayer.load()) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
            event.kickMessage = Configurations.getConfigMessage("error.player-load")
        }

        PlayerManager.updateUUIDCache(event.name, event.uniqueId)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onQuitSave(event: PlayerQuitEvent) {
        // Deletes every trace of the player.
        PlayerManager.removeOnlinePlayer(event.player.uniqueId)?.apply { onDisconnect() }
    }

}