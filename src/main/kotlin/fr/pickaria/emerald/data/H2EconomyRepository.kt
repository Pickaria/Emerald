package fr.pickaria.emerald.data

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class H2EconomyRepository : EconomyRepository {
    override fun depositPlayer(accountId: UUID, amount: Double, currency: String) {
        transaction {
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
        return try {
            val account = transaction {
                Account.find {
                    (Accounts.playerUuid eq accountId) and (Accounts.accountName eq currency)
                }.single()
            }

            account.balance
        } catch (_: NoSuchElementException) {
            0.0 // Empty
        } catch (_: IllegalArgumentException) {
            0.0 // More than one
        }
    }

    override fun createPlayerAccount(accountId: UUID, currency: String) {
        // TODO("Not yet implemented")
    }

    override fun getFormat(): String {
        return ""
    }

    override fun currencyNamePlural(): String {
        return ""
    }

    override fun currencyNameSingular(): String {
        return ""
    }
}