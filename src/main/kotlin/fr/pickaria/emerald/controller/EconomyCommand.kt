package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import fr.pickaria.emerald.data.UnknownCurrencyException
import fr.pickaria.emerald.domain.Currencies
import fr.pickaria.emerald.domain.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.shared.give
import org.bukkit.entity.Player

@CommandAlias("eco|economy")
@CommandPermission("pickaria.admin.eco")
class EconomyCommand(private val economyService: EconomyService) : BaseCommand() {
    @Default
    @Subcommand("physical give")
    fun onDefault(player: Player, @Default("credits") currency: Currencies, @Default("1") amount: Double) {
        try {
            val itemStack = economyService.getPhysicalCurrency(Price(amount, currency))
            player.give(itemStack)
        } catch (e: UnknownCurrencyException) {
            throw InvalidCommandArgument(e.message)
        }
    }
}