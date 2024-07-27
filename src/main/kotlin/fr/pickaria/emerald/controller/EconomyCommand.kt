package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.shared.give
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

@CommandAlias("eco|economy")
@CommandPermission("pickaria.eco")
internal class EconomyCommand(
    private val economyService: EconomyService,
    private val messager: Messager,
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
        fun onVirtualGive(target: OfflinePlayer, @Default("1") amount: Double, @Default("credits") currency: String) {
            try {
                if (amount == 0.0) {
                    throw InvalidCommandArgument("Amount must not be zero")
                }

                val account = economyService.getAccount(target.uniqueId)
                val price = Price(amount, currency)
                if (amount > 0.0) {
                    account.deposit(price)
                } else {
                    account.withdraw(price)
                }
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
    fun onBalance(sender: CommandSender, name: String, @Default("credits") currency: String) {
        try {
            val balance = economyService.getAccount(name).getBalance(currency)
            val formattedBalance = economyService.format(balance)
            messager.info(sender, "balance.of.player", Text(name), Text(formattedBalance))
        } catch (e: UnknownCurrencyException) {
            messager.error(sender, "unknown.currency", Text(e.currency))
        }
    }
}
