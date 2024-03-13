package fr.pickaria.emerald.domain

import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.bukkit.OfflinePlayer

class Account internal constructor(
    private val economyRepository: EconomyRepository,
    val owner: OfflinePlayer
) {
    fun getBalance(currency: Currencies): Price {
        val balance = economyRepository.getBalance(owner.uniqueId, currency.serialName)
        return Price(balance, currency)
    }

    fun withdraw(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        val balance = economyRepository.getBalance(owner.uniqueId, price.currency.serialName)
        if (balance < price.amount) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(owner.uniqueId, price.amount, price.currency.serialName)
        AccountChangeEvent(this).callEvent()
    }

    fun deposit(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        economyRepository.depositPlayer(owner.uniqueId, price.amount, price.currency.serialName)
        AccountChangeEvent(this).callEvent()
    }
}