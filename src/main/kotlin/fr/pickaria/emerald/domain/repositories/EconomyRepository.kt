package fr.pickaria.emerald.domain.repositories

import fr.pickaria.emerald.data.CurrencyConfig
import fr.pickaria.emerald.domain.UnknownCurrencyException
import java.util.*

internal interface EconomyRepository {
    fun depositPlayer(userName: String, accountId: UUID, amount: Double, currency: String)
    fun withdrawPlayer(userName: String, accountId: UUID, amount: Double, currency: String)
    fun getBalance(accountId: UUID, currency: String): Double
    fun getConfig(currency: String): CurrencyConfig
    fun getDefaultCurrencyKey(): String
    fun getAvailableCurrencyNames(): Set<String>
    fun getCollectionSoundKey(currency: String): String
    fun getOrCreateOwnerUniqueIdFromName(name: String): UUID
    fun getOwnerUniqueIdFromName(name: String): UUID?
    fun getBalanceTop(limit: Int, offset: Long, currency: String): List<Pair<String, Double>>
    fun getOwnerNameFromUniqueId(ownerUniqueId: UUID): String?

    /**
     * @throws UnknownCurrencyException
     */
    fun getStartAmountForCurrency(currency: String): Double
}
