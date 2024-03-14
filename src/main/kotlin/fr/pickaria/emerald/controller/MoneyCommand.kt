package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.BukkitCommandManager
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.UnknownCurrencyException
import org.bukkit.entity.Player

@CommandAlias("money|bal|balance")
@CommandPermission("pickaria.command.balance")
internal class MoneyCommand(manager: BukkitCommandManager, private val economyService: EconomyService) :
    BaseCommand() {
    init {
        manager.commandCompletions.registerCompletion("currencies") {
            economyService.getAvailableCurrencyNames()
        }
    }

    @Default
    @CommandCompletion("@currencies")
    fun onDefault(player: Player, @Default("credits") currency: String) {
        try {
            val balance = economyService.getAccount(player).getBalance(currency)
            val formattedBalance = economyService.format(balance)
            player.sendMessage("Balance: $formattedBalance")
        } catch (e: UnknownCurrencyException) {
            throw InvalidCommandArgument("Le compte '${e.currency}' n'existe pas.")
        }
    }
}
