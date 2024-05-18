package fr.pickaria.emerald.data

import fr.pickaria.database.openPool
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


internal class EconomyRepositoryImpl(private val config: EconomyConfig) : EconomyRepository {
    private val database: Database = openPool(config.database).also {
        transaction(it) {
            SchemaUtils.create(Accounts, UnregisteredUsers)
        }
    }

    override fun depositPlayer(userName: String, accountId: UUID, amount: Double, currency: String) {
        validateCurrencyName(currency)

        transaction(database) {
            createUserIfNotExists(userName, accountId).also {
                if (it.uniqueId != accountId) {
                    it.uniqueId = accountId
                }
            }

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

    override fun withdrawPlayer(userName: String, accountId: UUID, amount: Double, currency: String) {
        this.depositPlayer(userName, accountId, -amount, currency)
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

    override fun getDefaultCurrencyKey(): String = config.defaultCurrency

    override fun getAvailableCurrencyNames(): Set<String> = config.currencies.keys

    override fun getCollectionSoundKey(currency: String): String = getConfig(currency).collectionSound

    override fun getOwnerUniqueIdFromName(name: String): UUID {
        return transaction(database) {
            val unregisteredUser = createUserIfNotExists(
                name = name,
                uuid = UUID.nameUUIDFromBytes(name.toByteArray())
            )
            unregisteredUser.uniqueId
        }
    }

    private fun createUserIfNotExists(name: String, uuid: UUID): UnregisteredUser {
        return UnregisteredUser.find { UnregisteredUsers.username eq name }.singleOrNull() ?: UnregisteredUser.new {
            username = name
            uniqueId = uuid
        }
    }

    /**
     * @throws UnknownCurrencyException
     */
    private fun validateCurrencyName(currency: String) {
        if (!getAvailableCurrencyNames().contains(currency)) {
            throw UnknownCurrencyException(currency)
        }
    }
}
