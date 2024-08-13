package fr.pickaria.emerald.domain

data class BalanceTopAccount(
    val playerName: String,
    val balance: Price,
    val position: Int,
)
