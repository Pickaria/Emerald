package fr.pickaria.emerald.controller

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.AccountChangeEvent
import fr.pickaria.emerald.domain.AccountChangeType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal class NotifyListener(private val economyService: EconomyService) : Listener {
    @EventHandler
    fun onAccountChangeEvent(event: AccountChangeEvent) {
        event.account.owner.player?.let {
            val formattedPrice = economyService.format(event.price)
            it.playSound(economyService.getCollectionSound(event.price.currency))

            when (event.changeType) {
                AccountChangeType.WITHDRAW -> {
                    it.sendActionBar(Component.text("- $formattedPrice", NamedTextColor.RED))
                }

                AccountChangeType.DEPOSIT -> {
                    it.sendActionBar(Component.text("+ $formattedPrice", NamedTextColor.GOLD))
                }
            }
        }
    }
}
