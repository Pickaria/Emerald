package fr.pickaria.emerald.controller

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import fr.pickaria.bookshelf.commands.context.withPlayer
import fr.pickaria.bookshelf.commands.suggestion.StringSuggestionProvider
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.SuggestCommand
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.UnknownCurrencyException
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component

internal class MoneyCommand(
    private val economyService: EconomyService,
    private val messager: Messager,
) {
    fun register(commands: Commands) {
        commands.register(
            createBrigadierCommand(),
            "Displays the balance of a given currency",
            listOf("bal", "balance")
        )
    }

    private fun createBrigadierCommand(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("money")
            .requires {
                it.sender.hasPermission("pickaria.balance")
            }
            .executes {
                doExecute(it)
                Command.SINGLE_SUCCESS
            }
            .then(
                Commands.argument("currency", StringArgumentType.string())
                    .suggests(StringSuggestionProvider(economyService.getAvailableCurrencyNames()))
                    .executes {
                        doExecute(it)
                        Command.SINGLE_SUCCESS
                    }
            )
            .build()
    }

    private fun doExecute(context: CommandContext<CommandSourceStack>) {
        context.withPlayer(messager) { sender ->
            val currency = try {
                context.getArgument("currency", String::class.java)
            } catch (e: IllegalArgumentException) {
                economyService.getDefaultCurrency()
            }

            try {
                val balance = economyService.getAccount(sender).getBalance(currency)
                val formattedBalance = economyService.format(balance)
                messager.info(
                    sender,
                    "balance",
                    Text(formattedBalance),
                    SuggestCommand("/pay ", Component.translatable("send.money.to.player")),
                )
            } catch (e: UnknownCurrencyException) {
                messager.error(sender, "unknown.currency", Text(e.currency))
            }
        }
    }
}
