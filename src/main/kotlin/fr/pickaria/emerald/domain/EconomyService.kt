package fr.pickaria.emerald.domain

import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

interface EconomyService {
    fun format(price: Price): String
    fun getValueOfItem(item: ItemStack): Price
    fun deposit(player: OfflinePlayer, price: Price)
    fun withdraw(player: OfflinePlayer, price: Price)
    fun getBalance(player: OfflinePlayer, currency: Currencies): Price
    fun getPhysicalCurrency(price: Price): ItemStack
}
