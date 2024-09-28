package fr.pickaria.emerald

import fr.pickaria.emerald.domain.*
import net.kyori.adventure.sound.Sound
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

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
    fun getAccount(player: Player): Account

    /**
     * Retrieves an OfflinePlayer from a UUID.
     * @throws NullPointerException If no name is found for given UUID
     */
    fun getAccount(uniqueId: UUID): Account

    /**
     * Try getting an already registered account from a name.
     */
    fun tryGetAccount(name: String): Account?

    /**
     * **You are probably using this method wrong!**
     *
     * Getting an OfflinePlayer from a String name is expensive for the server and can cause crashes!
     *
     * @throws NullPointerException If the OfflinePlayer does not have a name
     */
    @Unsafe
    fun getAccount(player: OfflinePlayer): Account

    /**
     * **This method must not be used to get the account of a Player!**
     *
     * If you want to get the account of a Player, use this instead:
     * ```kt
     * val account: Account? = Bukkit.getPlayerExact(name)?.let { getAccount(it) }
     * ```
     *
     * Or for an offline Player that already has an account:
     * ```kt
     * val account: Account? = service.tryGetAccount(name)
     * ```
     */
    @Unsafe
    fun getAccount(name: String): Account

    /**
     * Creates a new transaction from players.
     */
    fun createTransaction(from: UUID, to: UUID, amount: Price): Transaction

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

    fun getDefaultCurrency(): String

    /**
     * @throws UnknownCurrencyException
     */
    fun getCollectionSound(currency: String): Sound
    fun getBalanceTop(page: Int, currency: String): List<BalanceTopAccount>

    /**
     * @throws UnknownCurrencyException
     * @throws InvalidAmountException
     */
    fun initializeAccount(player: Player)
}

@RequiresOptIn(message = "This API is unsafe to use. Read the documentation before using it.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class Unsafe
