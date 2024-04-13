package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.shared.give
import org.bukkit.entity.Player

@CommandAlias("eco|economy")
@CommandPermission("pickaria.eco")
internal class EconomyCommand(private val economyService: EconomyService) : BaseCommand() {
    @Subcommand("give")
    @CommandPermission("pickaria.eco.give")
    inner class Give : BaseCommand() {
        @Subcommand("virtual")
        @CommandPermission("pickaria.eco.give.virtual")
        fun onVirtualGive(player: Player, @Default("1") amount: Double, @Default("credits") currency: String) {
            try {
                economyService.getAccount(player).deposit(Price(amount, currency))
            } catch (e: UnknownCurrencyException) {
                throw InvalidCommandArgument(e.message)
            }
        }

        @Subcommand("physical")
        @CommandPermission("pickaria.eco.give.physical")
        fun onPhysicalGive(player: Player, @Default("1") amount: Double, @Default("credits") currency: String) {
            try {
                val itemStack = economyService.getValuableItemStack(Price(amount, currency))
                player.give(itemStack)
            } catch (e: UnknownCurrencyException) {
                throw InvalidCommandArgument(e.message)
            }
        }
    }
}
