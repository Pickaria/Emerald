package fr.pickaria.emerald.controller

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.InvalidAmountException
import fr.pickaria.emerald.domain.ItemIsNotACurrencyException
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

internal class CollectValuableListener(private val economyService: EconomyService) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val whoClicked = event.whoClicked

        if (whoClicked is Player) {
            event.currentItem?.let {
                if (depositItem(it, whoClicked)) {
                    event.inventory.remove(it)
                    whoClicked.inventory.remove(it)
                    whoClicked.updateInventory()

                    event.isCancelled = true
                    event.result = Event.Result.DENY
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (depositItem(event.itemDrop.itemStack, event.player)) {
            event.itemDrop.remove()
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        event.item?.let {
            if (depositItem(it, event.player)) {
                event.isCancelled = true
                event.setUseInteractedBlock(Event.Result.DENY)
                event.setUseItemInHand(Event.Result.DENY)
                it.amount -= 1
            }
        }
    }

    private fun depositItem(itemStack: ItemStack, player: Player): Boolean {
        return try {
            val price = economyService.getValueOfItem(itemStack)
            economyService.getAccount(player).deposit(price)
            itemStack.amount = 0
            true
        } catch (_: ItemIsNotACurrencyException) {
            false
        } catch (_: InvalidAmountException) {
            false
        }
    }
}
