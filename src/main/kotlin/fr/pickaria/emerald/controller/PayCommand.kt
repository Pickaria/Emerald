package fr.pickaria.emerald.controller

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import fr.pickaria.bookshelf.commands.context.getPlayer
import fr.pickaria.bookshelf.commands.context.withPlayer
import fr.pickaria.bookshelf.commands.suggestion.OnlinePlayerSuggestionProvider
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyPermissions
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.SenderCannotBeRecipientException
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

internal class PayCommand(
    private val economyService: EconomyService,
    private val messager: Messager,
) {
    fun register(commands: Commands) {
        commands.register(createBrigadierCommand(), "Sends a given amount of money to another player")
    }

    private fun createBrigadierCommand(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("pay")
            .requires {
                it.sender.hasPermission(EconomyPermissions.payCommand)
            }
            .then(
                Commands.argument("player", StringArgumentType.string())
                    .suggests(OnlinePlayerSuggestionProvider())
                    .then(
                        Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                            .executes {
                                doExecute(it)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .build()
    }

    private fun doExecute(context: CommandContext<CommandSourceStack>) {
        context.withPlayer(messager) { sender ->
            val recipient = context.getPlayer(messager, "player") ?: return@withPlayer

            val amount: Double = try {
                context.getArgument("amount", Double::class.java)
            } catch (_: IllegalArgumentException) {
                messager.error(sender, "pay.invalid.amount")
                return@withPlayer
            }

            val price = Price.roundedToTwoDecimals(amount, economyService.getDefaultCurrency())
            if (price.amount <= 0.01) {
                messager.error(sender, "pay.invalid.amount")
                return@withPlayer
            }

            try {
                economyService.createTransaction(sender.uniqueId, recipient.uniqueId, price).perform()
            } catch (_: BalanceInsufficientException) {
                messager.error(sender, "pay.balance.insufficient")
                return@withPlayer
            } catch (_: SenderCannotBeRecipientException) {
                messager.error(sender, "pay.cannot.self")
                return@withPlayer
            }

            val formattedPrice = economyService.format(price)
            messager.info(sender, "pay.sent", Text(recipient.name()), Text(formattedPrice))
            messager.info(recipient, "pay.received", Text(sender.name()), Text(formattedPrice))
        }
    }
}
