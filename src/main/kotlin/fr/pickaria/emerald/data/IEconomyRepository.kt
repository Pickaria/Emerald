package fr.pickaria.emerald.data

import java.util.*

interface IEconomyRepository<T> where T : Enum<T> {
    fun depositPlayer(accountId: UUID, currency: T, amount: Double)
    fun withdrawPlayer(accountId: UUID, currency: T, amount: Double)
    fun getBalance(accountId: UUID, currency: T): Double
    fun createPlayerAccount(accountId: UUID, currency: T)
    fun getFormat(): String
    fun currencyNamePlural(): String
    fun currencyNameSingular(): String
}