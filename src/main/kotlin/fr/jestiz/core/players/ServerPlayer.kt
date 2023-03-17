package fr.jestiz.core.players

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*


class ServerPlayer(uuid: UUID) : OfflineServerPlayer(uuid) {

    private val temporaryHiddenPlayers = listOf<UUID>()

    val player: Player
        get() = Bukkit.getPlayer(uuid)

    override fun load(): Boolean {
        if (PlayerManager.offlinePlayerExists(uuid)) {
            transferInstance(PlayerManager.getOfflinePlayer(uuid))
            return true
        }

        return super.load()
    }

    override fun writeToRedis(): Boolean {
        super.writeToRedis()

        PlayerManager.updateUUIDCache(super.bukkitPlayer.name, uuid)

        return true
    }

}