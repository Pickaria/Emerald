package fr.pickaria.emerald

import fr.pickaria.emerald.domain.*
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

/**
 * The main service of the plugin.
 *
 * Example:
 * ```kt
 * import fr.pickaria.emerald.EconomyService
 * val economyService: EconomyService? = server.servicesManager.getRegistration(EconomyService::class.java)?.provider
 * ```
 */
interface EconomyService {
    /**
     * Formats the Price to a human-readable String.
     * @throws UnknownCurrencyException
     */
    fun format(price: Price): String

    /**
     * Gets the player's bank account.
     */
    fun getAccount(player: OfflinePlayer): Account

    /**
     * Creates a new transaction from players.
     */
    fun createTransaction(from: OfflinePlayer, to: OfflinePlayer, amount: Price): Transaction

    /**
     * Gets the Price value of a valuable item.
     * @throws InvalidAmountException
     * @throws UnknownCurrencyException
     */
    fun getValueOfItem(item: ItemStack): Price

    /**
     * Gets the ItemStack for the given Price.
     * @throws InvalidAmountException
     * @throws UnknownCurrencyException
     */
    fun getValuableItemStack(price: Price): ItemStack

    /**
     * Gets a list of all valid configured currency names.
     */
    fun getAvailableCurrencyNames(): Set<String>
}
