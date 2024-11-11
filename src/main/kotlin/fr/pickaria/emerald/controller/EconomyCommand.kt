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
import fr.pickaria.emerald.EconomyPermissions
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.Account
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
            .requires { it.sender.hasPermission(EconomyPermissions.ecoCommand) }
            .then(createGiveCommand())
            .then(createBalanceCommand())
            .build()
    }

    private fun createGiveCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("give")
            .requires { it.sender.hasPermission(EconomyPermissions.ecoGiveCommand) }
            .then(createVirtualGiveCommand())
            .then(createPhysicalGiveCommand())
    }

    private fun createVirtualGiveCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("virtual")
            .requires { it.sender.hasPermission(EconomyPermissions.ecoGiveVirtualCommand) }
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
            .requires { it.sender.hasPermission(EconomyPermissions.ecoGivePhysicalCommand) }
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
            .requires { it.sender.hasPermission(EconomyPermissions.ecoBalanceCommand) }
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
        val sender = context.source.sender
        try {
            val amount = context.getArgument("amount", Double::class.java)

            if (amount <= 0.01) {
                messager.error(sender, "pay.invalid.amount")
                return
            }

            val currency = context.getArgument("currency", String::class.java)
            val account = getAccount(context)
            if (account == null) {
                messager.error(sender, "account.not.found")
                return
            }

            val price = Price(amount, currency)
            if (amount > 0.0) {
                account.deposit(price)
            } else {
                account.withdraw(price)
            }
        } catch (e: UnknownCurrencyException) {
            messager.error(sender, "unknown.currency", Text(e.currency))
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
            messager.error(context.source.sender, "unknown.currency", Text(e.currency))
        }
    }

    private fun doBalanceExecute(context: CommandContext<CommandSourceStack>) {
        val sender = context.source.sender
        try {
            val account = getAccount(context)
            if (account == null) {
                messager.error(sender, "account.not.found")
                return
            }

            val currency = context.getArgument("currency", String::class.java)
            val balance = account.getBalance(currency)
            val formattedBalance = economyService.format(balance)
            messager.info(
                sender,
                "balance.of.player",
                Text(account.getName()),
                Text(formattedBalance)
            )
        } catch (e: UnknownCurrencyException) {
            messager.error(sender, "unknown.currency", Text(e.currency))
        }
    }

    private fun getTarget(context: CommandContext<CommandSourceStack>): Player? {
        val sender = context.source.sender
        val playerName = context.getArgument("target", String::class.java)
        val player = sender.server.getPlayerExact(playerName)
        if (player == null) {
            messager.error(sender, "player.not.found", Text(playerName))
        }
        return player
    }

    private fun getAccount(context: CommandContext<CommandSourceStack>): Account? {
        val targetName = context.getArgument("target", String::class.java)
        return economyService.tryGetAccount(targetName)
    }
}
