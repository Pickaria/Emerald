package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.BukkitCommandManager
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import fr.pickaria.emerald.data.UnknownCurrencyException
import fr.pickaria.emerald.domain.Currencies
import fr.pickaria.emerald.domain.EconomyService
import org.bukkit.entity.Player

@CommandAlias("money|bal|balance")
@CommandPermission("pickaria.command.balance")
class MoneyCommand(manager: BukkitCommandManager, private val economyService: EconomyService) :
    BaseCommand() {
    init {
        manager.commandContexts.registerContext(Currencies::class.java) {
            val arg = it.popFirstArg()
            try {
                Currencies.valueOf(arg.uppercase())
            } catch (e: IllegalArgumentException) {
                throw InvalidCommandArgument("Le compte '$arg' n'existe pas.")
            }
        }

        manager.commandCompletions.registerCompletion("currencies") {
            Currencies.entries.map { it.name.lowercase() }
        }
    }

    @Default
    @CommandCompletion("@currencies")
    fun onDefault(player: Player, @Default("credits") currency: Currencies) {
        try {
            val balance = economyService.getBalance(player, currency)
            val formattedBalance = economyService.format(balance)
            player.sendMessage("Balance: $formattedBalance")
        } catch (e: UnknownCurrencyException) {
            throw InvalidCommandArgument("Le compte '${e.currency}' n'existe pas.")
        }
    }
}
