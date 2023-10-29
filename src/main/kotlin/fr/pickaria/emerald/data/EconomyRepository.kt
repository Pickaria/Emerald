package fr.pickaria.emerald.data

import java.util.*

interface EconomyRepository {
    fun depositPlayer(accountId: UUID, amount: Double, currency: String)
    fun withdrawPlayer(accountId: UUID, amount: Double, currency: String)
    fun getBalance(accountId: UUID, currency: String): Double
    fun getConfig(currency: String): CurrencyConfig
}