package fr.pickaria.emerald.domain

import fr.pickaria.emerald.data.IEconomyRepository
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class Currencies {
    Credits,
    Shards,
    Keys,
}

class EconomyService(val economyRepository: IEconomyRepository<Currencies>): IEconomyService<Currencies> {
    override fun format(price: Price<Currencies>): String {
        TODO("Not yet implemented")
    }

    override fun getValueOfItem(item: ItemStack): Price<Currencies> {
        TODO("Not yet implemented")
    }

    override fun getPhysicalCurrency(price: Price<Currencies>): ItemStack {
        TODO("Not yet implemented")
    }

    override fun getBalance(player: Player, currency: Currencies): Price<Currencies> {
        val balance = economyRepository.getBalance(player.uniqueId, currency)
        return Price(balance, currency)
    }

    override fun withdrawAndAnnounce(player: Player, price: Price<Currencies>) {
        TODO("Not yet implemented")
    }

    override fun depositAndAnnounce(player: Player, price: Price<Currencies>) {
        TODO("Not yet implemented")
    }
}