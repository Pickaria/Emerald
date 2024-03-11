package fr.pickaria.emerald.domain

import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

public interface EconomyService {
    fun format(price: Price): String
    fun getValueOfItem(item: ItemStack): Price
    fun getAccount(player: OfflinePlayer): Account
    fun getPhysicalCurrency(price: Price): ItemStack
}
