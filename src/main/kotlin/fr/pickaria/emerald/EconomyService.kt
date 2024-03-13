package fr.pickaria.emerald

import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.Price
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

/**
 * The main service of the plugin.
 *
 * Sample code to retrieve it:
 * ```kt
 * import fr.pickaria.emerald.PaperEconomyService
 * val economyService: EconomyService? = server.servicesManager.getRegistration(EconomyService::class.java)?.provider
 * ```
 */
interface EconomyService {
    /**
     * Formats the Price to a human-readable String.
     */
    fun format(price: Price): String

    /**
     * Gets the player's bank account.
     */
    fun getAccount(player: OfflinePlayer): Account

    /**
     * Gets the Price value of a valuable item.
     */
    fun getValueOfItem(item: ItemStack): Price

    /**
     * Gets the ItemStack for the given Price.
     */
    fun getValuableItemStack(price: Price): ItemStack
}
