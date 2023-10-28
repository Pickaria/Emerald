package fr.pickaria.emerald.domain

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface IEconomyService<T> where T : Enum<T> {
    fun format(price: Price<T>): String
    fun getValueOfItem(item: ItemStack): Price<T>
    fun depositAndAnnounce(player: Player, price: Price<T>)
    fun withdrawAndAnnounce(player: Player, price: Price<T>)
    fun getBalance(player: Player, currency: T): Price<T>
    fun getPhysicalCurrency(price: Price<T>): ItemStack
}
