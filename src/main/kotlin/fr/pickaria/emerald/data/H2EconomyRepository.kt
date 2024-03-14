package fr.pickaria.emerald.data

import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


internal class H2EconomyRepository(private val config: EconomyConfig) : EconomyRepository {
    private val database: Database = openDatabase(config.database.url, config.database.driver)

    override fun depositPlayer(accountId: UUID, amount: Double, currency: String) {
        validateCurrencyName(currency)

        transaction(database) {
            try {
                val account = Account.find {
                    (Accounts.playerUuid eq accountId) and (Accounts.accountName eq currency)
                }.single()
                account.balance += amount
            } catch (_: NoSuchElementException) {
                // Account not found, create a new one
                Account.new {
                    playerUuid = accountId
                    accountName = currency
                    balance = amount
                }
            }
        }
    }

    override fun withdrawPlayer(accountId: UUID, amount: Double, currency: String) {
        this.depositPlayer(accountId, -amount, currency)
    }

    override fun getBalance(accountId: UUID, currency: String): Double {
        validateCurrencyName(currency)

        return try {
            val account = transaction(database) {
                Account.find {
                    (Accounts.playerUuid eq accountId) and (Accounts.accountName eq currency)
                }.single()
            }

            account.balance
        } catch (_: NoSuchElementException) {
            0.0 // Empty
        }
    }

    /**
     * @throws UnknownCurrencyException
     */
    override fun getConfig(currency: String): CurrencyConfig =
        config.currencies[currency] ?: throw UnknownCurrencyException(currency)

    override fun getDefaultCurrencyName(): String = config.defaultCurrency

    override fun getAvailableCurrencyNames(): Set<String> = config.currencies.keys

    /**
     * @throws UnknownCurrencyException
     */
    private fun validateCurrencyName(currency: String) {
        if (!getAvailableCurrencyNames().contains(currency)) {
            throw UnknownCurrencyException(currency)
        }
    }
}
