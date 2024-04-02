package fr.pickaria.emerald.domain.repositories

import fr.pickaria.emerald.data.CurrencyConfig
import java.util.*

internal interface EconomyRepository {
    fun depositPlayer(accountId: UUID, amount: Double, currency: String)
    fun withdrawPlayer(accountId: UUID, amount: Double, currency: String)
    fun getBalance(accountId: UUID, currency: String): Double
    fun getConfig(currency: String): CurrencyConfig
    fun getDefaultCurrencyKey(): String
    fun getAvailableCurrencyNames(): Set<String>
    fun getCollectionSoundKey(currency: String): String
}
