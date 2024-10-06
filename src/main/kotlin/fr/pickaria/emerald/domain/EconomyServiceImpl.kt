package fr.pickaria.emerald.domain

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.Unsafe
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


internal class EconomyServiceImpl(
    private val economyRepository: EconomyRepository,
    private val plugin: JavaPlugin,
) : EconomyService {
    private val decimalFormat = DecimalFormat("###,##0.00", DecimalFormatSymbols(Locale.FRENCH)).also {
        it.roundingMode = RoundingMode.FLOOR
    }

    override fun format(price: Price): String {
        val currencyConfig = economyRepository.getConfig(price.currency)
        val formattedPrice = decimalFormat.format(price.amount)

        return if (price.amount == 1.0 || price.amount == 0.0) {
            "$formattedPrice ${currencyConfig.name.singular}"
        } else {
            "$formattedPrice ${currencyConfig.name.plural}"
        }
    }

    override fun getValueOfItem(item: ItemStack): Price {
        if (item.itemMeta == null) {
            // Happens for AIR material
            throw ItemIsNotACurrencyException()
        }

        val currency = item.itemMeta.persistentDataContainer.get(currencyNamespace, PersistentDataType.STRING)
        val value = item.itemMeta.persistentDataContainer.get(valueNamespace, PersistentDataType.DOUBLE)

        if (value == null || currency == null) {
            throw ItemIsNotACurrencyException()
        }

        try {
            economyRepository.getConfig(currency)
        } catch (e: UnknownCurrencyException) {
            throw ItemIsNotACurrencyException()
        }

        val totalValue = item.amount * value

        if (totalValue < 0.0) {
            throw InvalidAmountException(totalValue)
        }

        return Price(totalValue, currency)
    }

    override fun getValuableItemStack(price: Price): ItemStack {
        if (price.amount < 0.0) {
            throw InvalidAmountException(price.amount)
        }

        return ValuableItem(economyRepository, this, price)
            .getItem()
    }

    override fun initializeAccount(player: Player) {
        if (!player.hasPlayedBefore()) {
            economyRepository.getAvailableCurrencyNames().forEach {
                val startAmount = economyRepository.getStartAmountForCurrency(it)
                if (startAmount > 0) {
                    val account = getAccount(player)
                    account.deposit(Price(startAmount, it))
                }
                // Ignore the exceptions, it should never throw
            }
        }
    }

    // Despite the opt-in, getting an OfflinePlayer from a uniqueId is safe
    @OptIn(Unsafe::class)
    override fun getAccount(uniqueId: UUID): Account {
        val player = Bukkit.getOfflinePlayer(uniqueId)
        return getAccount(player)
    }

    override fun getAccount(player: Player): Account = getAccount(player.name, player.uniqueId)

    override fun tryGetAccount(name: String): Account? {
        val uniqueId = tryGetUniqueId(name)
        return uniqueId?.let { getAccount(name, uniqueId) }
    }

    @Unsafe
    override fun getAccount(player: OfflinePlayer): Account = getAccount(player.name!!, player.uniqueId)

    @Unsafe
    override fun getAccount(name: String): Account {
        val uniqueId = tryGetUniqueId(name) ?: economyRepository.getOrCreateOwnerUniqueIdFromName(name)
        return getAccount(name, uniqueId)
    }

    private fun getAccount(name: String, uniqueId: UUID): Account {
        return Account(
            economyRepository = economyRepository,
            name = name,
            ownerUniqueId = uniqueId,
            plugin = plugin,
        )
    }

    private fun tryGetUniqueId(name: String): UUID? {
        return Bukkit.getPlayerExact(name)?.uniqueId ?: economyRepository.getOwnerUniqueIdFromName(name)
    }

    override fun createTransaction(from: UUID, to: UUID, amount: Price): Transaction {
        val fromAccount = getAccount(from)
        val toAccount = getAccount(to)

        return Transaction(fromAccount, toAccount, amount)
    }

    override fun getAvailableCurrencyNames(): Set<String> = economyRepository.getAvailableCurrencyNames()

    override fun getCollectionSound(currency: String): Sound {
        val soundKey = economyRepository.getCollectionSoundKey(currency)
        return Sound.sound(Key.key(soundKey), Sound.Source.MASTER, 1f, 1f)
    }

    override fun getDefaultCurrency(): String = economyRepository.getDefaultCurrencyKey()

    override fun getBalanceTop(page: Int, currency: String): List<BalanceTopAccount> {
        val limit = 6
        val offset = page * limit
        return economyRepository.getBalanceTop(
            limit = limit,
            offset = offset.toLong(),
            currency = currency,
        )
            .mapIndexed { index, it ->
                val price = Price(it.second, currency)
                BalanceTopAccount(
                    playerName = it.first,
                    balance = price,
                    position = index + offset + 1,
                )
            }
    }
}

class InvalidAmountException(val amount: Double) : Throwable()
class ItemIsNotACurrencyException : Throwable()
class UnknownCurrencyException(val currency: String) : Throwable()
