package fr.pickaria.emerald.domain

import fr.pickaria.emerald.data.EconomyRepository
import fr.pickaria.emerald.data.UnknownCurrencyException
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.math.RoundingMode
import java.text.DecimalFormat

class PaperEconomyService(private val economyRepository: EconomyRepository) : EconomyService {
    private val currencyService = PhysicalCurrencyService(this, economyRepository)

    override fun format(price: Price): String {
        val currencyConfig = economyRepository.getConfig(price.currency.serialName)

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

        return Price(totalValue, Currencies.valueOf(currency.uppercase()))
    }

    override fun getPhysicalCurrency(price: Price): ItemStack {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        return currencyService.item(price)
    }

    override fun getBalance(player: OfflinePlayer, currency: Currencies): Price {
        val balance = economyRepository.getBalance(player.uniqueId, currency.serialName)
        return Price(balance, currency)
    }

    override fun withdraw(player: OfflinePlayer, price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        val balance = economyRepository.getBalance(player.uniqueId, price.currency.serialName)
        if (balance < price.amount) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(player.uniqueId, price.amount, price.currency.serialName)
    }

    override fun deposit(player: OfflinePlayer, price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        economyRepository.depositPlayer(player.uniqueId, price.amount, price.currency.serialName)
    }
}

class BalanceInsufficientException : Throwable()
class InvalidAmountException : Throwable()
class ItemIsNotACurrencyException : Throwable()
