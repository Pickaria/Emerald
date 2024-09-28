package fr.pickaria.emerald.controller

import fr.pickaria.emerald.EconomyService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

internal class CreateAccountListener(private val economyService: EconomyService) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        economyService.initializeAccount(event.player)
    }
}
