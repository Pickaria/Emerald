package fr.pickaria.emerald.domain

import fr.pickaria.emerald.data.EconomyRepository
import org.bukkit.OfflinePlayer

class Account internal constructor(private val economyRepository: EconomyRepository, private val player: OfflinePlayer) {
    fun getBalance(currency: Currencies): Price {
        val balance = economyRepository.getBalance(player.uniqueId, currency.serialName)
        return Price(balance, currency)
    }

    fun withdraw(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        val balance = economyRepository.getBalance(player.uniqueId, price.currency.serialName)
        if (balance < price.amount) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(player.uniqueId, price.amount, price.currency.serialName)
    }

    fun deposit(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        economyRepository.depositPlayer(player.uniqueId, price.amount, price.currency.serialName)
    }
}