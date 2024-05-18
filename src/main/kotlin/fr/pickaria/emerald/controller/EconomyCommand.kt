package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.shared.MiniMessage
import fr.pickaria.shared.give
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("eco|economy")
@CommandPermission("pickaria.eco")
internal class EconomyCommand(
    private val economyService: EconomyService,
    manager: PaperCommandManager
) : BaseCommand() {
    init {
        manager.commandCompletions.registerCompletion("amounts") {
            setOf("1.0")
        }
    }

    @Subcommand("give")
    @CommandPermission("pickaria.eco.give")
    inner class Give : BaseCommand() {
        @Subcommand("virtual")
        @CommandPermission("pickaria.eco.give.virtual")
        @CommandCompletion("@players @amounts @currencies")
        fun onVirtualGive(target: OnlinePlayer, @Default("1") amount: Double, @Default("credits") currency: String) {
            try {
                economyService.getAccount(target.getPlayer()).deposit(Price(amount, currency))
            } catch (e: UnknownCurrencyException) {
                throw InvalidCommandArgument(e.message)
            }
        }

        @Subcommand("physical")
        @CommandPermission("pickaria.eco.give.physical")
        @CommandCompletion("@players @amounts @currencies")
        fun onPhysicalGive(target: OnlinePlayer, @Default("1") amount: Double, @Default("credits") currency: String) {
            try {
                val itemStack = economyService.getValuableItemStack(Price(amount, currency))
                target.getPlayer().give(itemStack)
            } catch (e: UnknownCurrencyException) {
                throw InvalidCommandArgument(e.message)
            }
        }
    }

    @Subcommand("balance")
    @CommandPermission("pickaria.eco.balance")
    @CommandCompletion("@players @currencies")
    fun onBalance(sender: Player, name: String, @Default("credits") currency: String) {
        val player = Bukkit.getOfflinePlayer(name)

        try {
            val balance = economyService.getAccount(player).getBalance(currency)
            val formattedBalance = economyService.format(balance)
            MiniMessage("<gray>Solde de <name> (<uuid>) : <gold><value>") {
                "name" to name
                "uuid" to player.uniqueId.toString()
                "value" to formattedBalance
            }.send(sender)
        } catch (e: UnknownCurrencyException) {
            throw InvalidCommandArgument("Le compte '${e.currency}' n'existe pas.")
        }
    }
}
