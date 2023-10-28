package fr.pickaria.emerald.data

import java.util.*

interface EconomyRepository {
    fun depositPlayer(accountId: UUID, amount: Double, currency: String)
    fun withdrawPlayer(accountId: UUID, amount: Double, currency: String)
    fun getBalance(accountId: UUID, currency: String): Double
    fun getFormat(currency: String): String
    fun currencyNamePlural(currency: String): String
    fun currencyNameSingular(currency: String): String
    fun getPhysicalCurrencies(currency: String): List<PhysicalCurrency>
}