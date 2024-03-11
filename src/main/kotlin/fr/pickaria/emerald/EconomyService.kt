package fr.pickaria.emerald

import fr.pickaria.emerald.domain.Account
import fr.pickaria.emerald.domain.Price
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack

interface EconomyService {
    fun format(price: Price): String
    fun getValueOfItem(item: ItemStack): Price
    fun getAccount(player: OfflinePlayer): Account
    fun getPhysicalCurrency(price: Price): ItemStack
}
