package fr.pickaria.emerald.controller

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.Compound
import fr.pickaria.bookshelf.messager.components.Listing
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyService
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

internal class BalanceTopCommand(
    private val economyService: EconomyService,
    private val messager: Messager,
) {
    fun register(commands: Commands) {
        commands.register(createBrigadierCommand(), "Displays the top balance holders", listOf("baltop"))
    }

    private fun createBrigadierCommand(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("balancetop")
            .requires {
                it.sender.hasPermission("pickaria.balancetop")
            }
            .then(
                Commands.argument("page", IntegerArgumentType.integer(1))
                    .executes {
                        doExecute(it)
                        Command.SINGLE_SUCCESS
                    }
            )
            .executes {
                doExecute(it)
                Command.SINGLE_SUCCESS
            }
            .build()
    }

    private fun doExecute(context: CommandContext<CommandSourceStack>) {
        val sender = context.source.sender
        val page = try {
            context.getArgument("page", Int::class.java) - 1
        } catch (_: IllegalArgumentException) {
            0
        }

        val topBalances = economyService.getBalanceTop(page, economyService.getDefaultCurrency())
        val list = topBalances
            .map {
                Compound(
                    Text("${it.position}. ${it.playerName}, ", changeColor = false),
                    Text(economyService.format(it.balance))
                )
            }
        messager.info(sender, "balance.top", Listing(list))
    }
}