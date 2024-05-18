package fr.pickaria.emerald.domain

import fr.pickaria.emerald.domain.events.AccountChangeEvent
import fr.pickaria.emerald.domain.events.AccountChangeType
import fr.pickaria.emerald.domain.events.AsyncAccountChangeEvent
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


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
    private val name: String,
    val ownerUniqueId: UUID,
    private val plugin: JavaPlugin,
) {
    companion object {
        internal fun fromName(economyRepository: EconomyRepository, name: String, plugin: JavaPlugin): Account {
            val uniqueId = Bukkit.getPlayerExact(name)?.uniqueId ?: economyRepository.getOwnerUniqueIdFromName(name)
            return Account(
                economyRepository = economyRepository,
                ownerUniqueId = uniqueId,
                name = name,
                plugin = plugin,
            )
        }
    }

    /**
     * @throws UnknownCurrencyException
     */
    fun getBalance(currency: String): Price {
        val balance = economyRepository.getBalance(ownerUniqueId, currency)
        return Price(balance, currency)
    }

    /**
     * @throws UnknownCurrencyException
     * @throws InvalidAmountException
     */
    fun has(price: Price): Boolean {
        if (price.amount < 0.0) {
            Bukkit.getLogger().severe("An account cannot have a negative amount of: ${price.amount} ${price.currency}")
            throw InvalidAmountException(price.amount)
        }

        val balance = economyRepository.getBalance(ownerUniqueId, price.currency)
        return balance >= price.amount
    }

    /**
     * @throws UnknownCurrencyException
     * @throws BalanceInsufficientException
     * @throws InvalidAmountException
     */
    fun withdraw(price: Price) {
        if (!has(price)) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(name, ownerUniqueId, price.amount, price.currency)
        callAccountChangeEvent(price, AccountChangeType.WITHDRAW)
    }

    /**
     * @throws UnknownCurrencyException
     * @throws BalanceInsufficientException
     * @throws InvalidAmountException
     */
    fun withdrawAll(prices: Iterable<Price>) {
        if (!prices.all { has(it) }) {
            throw BalanceInsufficientException()
        }

        val withdrew = mutableListOf<Price>()

        try {
            prices.forEach {
                withdraw(it)
                withdrew.add(it)
            }
        } catch (_: BalanceInsufficientException) {
            withdrew.reversed().forEach {
                deposit(it)
            }

            throw BalanceInsufficientException()
        }
    }

    /**
     * @throws UnknownCurrencyException
     * @throws InvalidAmountException
     */
    fun deposit(price: Price) {
        if (price.amount < 0.0) {
            throw InvalidAmountException(price.amount)
        }

        economyRepository.depositPlayer(name, ownerUniqueId, price.amount, price.currency)
        callAccountChangeEvent(price, AccountChangeType.DEPOSIT)
    }

    private fun callAccountChangeEvent(price: Price, eventType: AccountChangeType) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val event = AsyncAccountChangeEvent(this, price, eventType)
            Bukkit.getPluginManager().callEvent(event)
        })

        try {
            val event = AccountChangeEvent(this, price, eventType)
            Bukkit.getPluginManager().callEvent(event)
        } catch (_: IllegalStateException) {
        }
    }
}

class BalanceInsufficientException : Throwable()
