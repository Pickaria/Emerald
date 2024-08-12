package fr.pickaria.emerald.controller

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import fr.pickaria.bookshelf.commands.suggestion.OnlinePlayerSuggestionProvider
import fr.pickaria.bookshelf.commands.suggestion.StringSuggestionProvider
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.shared.give
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player

internal class EconomyCommand(
    private val economyService: EconomyService,
    private val messager: Messager,
) {
    fun register(commands: Commands) {
        commands.register(createBrigadierCommand(), "Economic management command")
    }

    private fun createBrigadierCommand(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("eco")
            .requires { it.sender.hasPermission("pickaria.eco") }
            .then(createGiveCommand())
            .then(createBalanceCommand())
            .build()
    }

    private fun createGiveCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("give")
            .requires { it.sender.hasPermission("pickaria.eco.give") }
            .then(createVirtualGiveCommand())
            .then(createPhysicalGiveCommand())
    }

    private fun createVirtualGiveCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("virtual")
            .requires { it.sender.hasPermission("pickaria.eco.give.virtual") }
            .then(
                Commands.argument("target", StringArgumentType.string())
                    .suggests(OnlinePlayerSuggestionProvider())
                    .then(
                        Commands.argument("amount", DoubleArgumentType.doubleArg(1.0))
                            .then(
                                Commands.argument("currency", StringArgumentType.string())
                                    .suggests(StringSuggestionProvider(economyService.getAvailableCurrencyNames()))
                                    .executes {
                                        doVirtualGiveExecute(it)
                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
            )
    }

    private fun createPhysicalGiveCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("physical")
            .requires { it.sender.hasPermission("pickaria.eco.give.physical") }
            .then(
                Commands.argument("target", StringArgumentType.string())
                    .suggests(OnlinePlayerSuggestionProvider())
                    .then(
                        Commands.argument("amount", DoubleArgumentType.doubleArg(1.0))
                            .then(
                                Commands.argument("currency", StringArgumentType.string())
                                    .suggests(StringSuggestionProvider(economyService.getAvailableCurrencyNames()))
                                    .executes {
                                        doPhysicalGiveExecute(it)
                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
            )
    }

    private fun createBalanceCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("balance")
            .requires { it.sender.hasPermission("pickaria.eco.balance") }
            .then(
                Commands.argument("target", StringArgumentType.string())
                    .suggests(OnlinePlayerSuggestionProvider())
                    .then(
                        Commands.argument("currency", StringArgumentType.string())
                            .suggests(StringSuggestionProvider(economyService.getAvailableCurrencyNames()))
                            .executes {
                                doBalanceExecute(it)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
    }

    private fun doVirtualGiveExecute(context: CommandContext<CommandSourceStack>) {
        val target = getTarget(context) ?: return
        val amount = context.getArgument("amount", Double::class.java)
        val currency = context.getArgument("currency", String::class.java)

        try {
            if (amount == 0.0) {
                throw IllegalArgumentException("Amount must not be zero")
            }

            val account = economyService.getAccount(target.uniqueId)
            val price = Price(amount, currency)
            if (amount > 0.0) {
                account.deposit(price)
            } else {
                account.withdraw(price)
            }
        } catch (e: UnknownCurrencyException) {
            messager.error(context.source.sender, e.message ?: "Unknown error")
        }
    }

    private fun doPhysicalGiveExecute(context: CommandContext<CommandSourceStack>) {
        val target = getTarget(context) ?: return
        val amount = context.getArgument("amount", Double::class.java)
        val currency = context.getArgument("currency", String::class.java)

        try {
            val itemStack = economyService.getValuableItemStack(Price(amount, currency))
            target.give(itemStack)
        } catch (e: UnknownCurrencyException) {
            messager.error(context.source.sender, e.message ?: "Unknown error")
        }
    }

    private fun doBalanceExecute(context: CommandContext<CommandSourceStack>) {
        val target = getTarget(context) ?: return
        val currency = context.getArgument("currency", String::class.java)

        try {
            val balance = economyService.getAccount(target.uniqueId).getBalance(currency)
            val formattedBalance = economyService.format(balance)
            messager.info(context.source.sender, "balance.of.player", Text(target.name), Text(formattedBalance))
        } catch (e: UnknownCurrencyException) {
            messager.error(context.source.sender, "unknown.currency", Text(e.currency))
        }
    }

    private fun getTarget(context: CommandContext<CommandSourceStack>): Player? {
        val playerName = context.getArgument("target", String::class.java)
        val player = context.source.sender.server.getPlayerExact(playerName)
        if (player == null) {
            messager.error(context.source.sender, "player.not.found", Text(playerName))
        }
        return player
    }
}
