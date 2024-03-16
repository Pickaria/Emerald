package fr.pickaria.emerald.domain

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.math.RoundingMode
import java.text.DecimalFormat

internal class PaperEconomyService(private val economyRepository: EconomyRepository) : EconomyService {
    override fun format(price: Price): String {
        val currencyConfig = economyRepository.getConfig(price.currency)

        val decimalFormat = DecimalFormat(currencyConfig.format).also {
            it.roundingMode = RoundingMode.FLOOR
        }
        val formattedPrice = decimalFormat.format(price.amount)

        return if (price.amount <= 1.0) {
            "$formattedPrice ${currencyConfig.nameSingular}"
        } else {
            "$formattedPrice ${currencyConfig.namePlural}"
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

        if (totalValue <= 0.0) {
            throw InvalidAmountException()
        }

        return Price(totalValue, currency)
    }

    override fun getValuableItemStack(price: Price): ItemStack {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        return ValuableItem(economyRepository, this, price)
            .getItem()
    }

    override fun getAccount(player: OfflinePlayer): Account =
        Account(
            economyRepository = economyRepository,
            owner = player,
        )

    override fun createTransaction(from: OfflinePlayer, to: OfflinePlayer, amount: Price): Transaction {
        val fromAccount = getAccount(from)
        val toAccount = getAccount(to)

        return Transaction(fromAccount, toAccount, amount)
    }

    override fun getAvailableCurrencyNames(): Set<String> = economyRepository.getAvailableCurrencyNames()
}

class InvalidAmountException : Throwable()
class ItemIsNotACurrencyException : Throwable()
class UnknownCurrencyException(val currency: String) : Throwable()
