package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.BukkitCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.SuggestCommand
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.UnknownCurrencyException
import org.bukkit.entity.Player

@CommandAlias("money|bal|balance")
@CommandPermission("pickaria.balance")
internal class MoneyCommand(
    manager: BukkitCommandManager,
    private val economyService: EconomyService,
    private val messager: Messager,
) : BaseCommand() {
    init {
        manager.commandCompletions.registerCompletion("currencies") {
            economyService.getAvailableCurrencyNames()
        }
    }

    @Default
    @CommandCompletion("@currencies")
    fun onDefault(sender: Player, @Default("credits") currency: String) {
        try {
            val balance = economyService.getAccount(sender).getBalance(currency)
            val formattedBalance = economyService.format(balance)
            messager.info(
                sender,
                "balance",
                Text(formattedBalance),
                SuggestCommand("/pay ", "Envoyer de l'argent à un joueur"),
            )
        } catch (e: UnknownCurrencyException) {
            messager.error(sender, "unknown.currency", Text(e.currency))
        }
    }
}
