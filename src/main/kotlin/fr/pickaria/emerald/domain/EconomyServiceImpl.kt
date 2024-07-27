package fr.pickaria.emerald.domain

import fr.pickaria.emerald.EconomyService
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
import kotlin.math.pow


internal class EconomyServiceImpl(
    private val economyRepository: EconomyRepository,
    private val plugin: JavaPlugin,
) : EconomyService {
    override fun format(price: Price): String {
        val currencyConfig = economyRepository.getConfig(price.currency)

        val symbols = DecimalFormatSymbols(Locale.FRENCH)
        val decimalFormat = DecimalFormat(currencyConfig.format, symbols).also {
            it.roundingMode = RoundingMode.FLOOR
        }

        val isFractional = price.amount < 1.0 && price.amount > -1.0

        val formattedPrice = if (isFractional) {
            decimalFormat.minimumFractionDigits = 0
            decimalFormat.format(price.amount * 10.0.pow(decimalFormat.maximumFractionDigits))
        } else {
            decimalFormat.format(price.amount)
        }

        return if (price.amount == 1.0 || price.amount == 0.0) {
            "$formattedPrice ${currencyConfig.name.singular}"
        } else if (isFractional) {
            "$formattedPrice ${currencyConfig.fractionalName.plural}"
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

    override fun getAccount(player: OfflinePlayer): Account =
        Account(
            economyRepository = economyRepository,
            name = player.name!!,
            ownerUniqueId = player.uniqueId,
            plugin = plugin,
        )

    override fun getAccount(uniqueId: UUID): Account {
        val player = Bukkit.getOfflinePlayer(uniqueId)
        return Account(
            economyRepository = economyRepository,
            name = player.name!!,
            ownerUniqueId = player.uniqueId,
            plugin = plugin,
        )
    }

    override fun getAccount(player: Player): Account =
        Account(
            economyRepository = economyRepository,
            name = player.name,
            ownerUniqueId = player.uniqueId,
            plugin = plugin,
        )

    override fun getAccount(name: String): Account =
        Account.fromName(
            economyRepository = economyRepository,
            name = name,
            plugin = plugin,
        )

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
}

class InvalidAmountException(val amount: Double) : Throwable()
class ItemIsNotACurrencyException : Throwable()
class UnknownCurrencyException(val currency: String) : Throwable()
