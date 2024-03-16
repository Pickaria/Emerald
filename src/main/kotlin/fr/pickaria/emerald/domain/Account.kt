package fr.pickaria.emerald.domain

import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.bukkit.OfflinePlayer

/**
 * Represents a player's bank account.
 *
 * Example:
 * ```kt
 * val account: Account = economyService.getAccount(player)
 * ```
 */
class Account internal constructor(
    private val economyRepository: EconomyRepository,
    val owner: OfflinePlayer
) {
    /**
     * @throws UnknownCurrencyException
     */
    fun getBalance(currency: String): Price {
        val balance = economyRepository.getBalance(owner.uniqueId, currency)
        return Price(balance, currency)
    }

    /**
     * @throws UnknownCurrencyException
     * @throws BalanceInsufficientException
     * @throws InvalidAmountException
     */
    fun withdraw(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        val balance = economyRepository.getBalance(owner.uniqueId, price.currency)
        if (balance < price.amount) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(owner.uniqueId, price.amount, price.currency)
        AccountChangeEvent(this, price, AccountChangeType.WITHDRAW).callEvent()
    }

    /**
     * @throws UnknownCurrencyException
     * @throws InvalidAmountException
     */
    fun deposit(price: Price) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        economyRepository.depositPlayer(owner.uniqueId, price.amount, price.currency)
        AccountChangeEvent(this, price, AccountChangeType.DEPOSIT).callEvent()
    }
}

class BalanceInsufficientException : Throwable()
