package fr.pickaria.emerald.domain

import fr.pickaria.emerald.data.EconomyRepository
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.math.RoundingMode
import java.text.DecimalFormat

class PaperEconomyService(private val economyRepository: EconomyRepository): EconomyService<Currencies> {
    override fun format(price: Price<Currencies>): String {
        val format = economyRepository.getFormat(price.currency.name)
        val decimalFormat = DecimalFormat(format).also {
            it.roundingMode = RoundingMode.FLOOR
        }
        val formattedPrice = decimalFormat.format(price.amount)

        return if (price.amount <= 1.0) {
            "$formattedPrice ${economyRepository.currencyNameSingular(price.currency.name)}"
        } else {
            "$formattedPrice ${economyRepository.currencyNamePlural(price.currency.name)}"
        }
    }

    override fun getValueOfItem(item: ItemStack): Price<Currencies> {
        TODO("Not yet implemented")
    }

    override fun getPhysicalCurrency(price: Price<Currencies>): ItemStack {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        TODO("Not yet implemented")
    }

    override fun getBalance(player: Player, currency: Currencies): Price<Currencies> {
        val balance = economyRepository.getBalance(player.uniqueId, currency.name)
        return Price(balance, currency)
    }

    override fun withdrawAndAnnounce(player: Player, price: Price<Currencies>) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        val balance = economyRepository.getBalance(player.uniqueId, price.currency.name)
        if (balance < price.amount) {
            throw BalanceInsufficientException()
        }

        economyRepository.withdrawPlayer(player.uniqueId, price.amount, price.currency.name)

        player.sendActionBar { Component.text("") }
    }

    override fun depositAndAnnounce(player: Player, price: Price<Currencies>) {
        if (price.amount <= 0.0) {
            throw InvalidAmountException()
        }

        economyRepository.depositPlayer(player.uniqueId, price.amount, price.currency.name)

        player.sendActionBar { Component.text("") }
    }
}

class BalanceInsufficientException: Throwable()
class InvalidAmountException: Throwable()
