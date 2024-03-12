package fr.pickaria.emerald.domain

import fr.pickaria.emerald.domain.repositories.EconomyRepository
import java.util.UUID

class Account internal constructor(private val economyRepository: EconomyRepository, private val accountId: UUID) {
    fun getBalance(currency: Currencies): Price {
        val balance = economyRepository.getBalance(accountId, currency.serialName)
        return Price(balance, currency)
    }

    fun withdraw(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        val balance = economyRepository.getBalance(accountId, price.currency.serialName)
        if (balance < price.amount) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(accountId, price.amount, price.currency.serialName)
    }

    fun deposit(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        economyRepository.depositPlayer(accountId, price.amount, price.currency.serialName)
    }
}