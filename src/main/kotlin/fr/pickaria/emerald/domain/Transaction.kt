package fr.pickaria.emerald.domain

import org.bukkit.OfflinePlayer

data class Transaction(val from: OfflinePlayer, val to: OfflinePlayer, val amount: Price)
