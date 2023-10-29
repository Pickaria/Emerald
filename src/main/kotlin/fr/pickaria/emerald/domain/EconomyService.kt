package fr.pickaria.emerald.domain

import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

interface EconomyService<T> where T : Enum<T> {
    fun format(price: Price<T>): String
    fun getValueOfItem(item: ItemStack): Price<T>
    fun deposit(player: OfflinePlayer, price: Price<T>)
    fun withdraw(player: OfflinePlayer, price: Price<T>)
    fun getBalance(player: OfflinePlayer, currency: T): Price<T>
    fun getPhysicalCurrency(price: Price<T>): ItemStack
}
