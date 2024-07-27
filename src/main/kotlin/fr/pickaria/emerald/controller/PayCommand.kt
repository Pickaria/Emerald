package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.bookshelf.messager.components.Text
import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.SenderCannotBeRecipientException
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("pay")
@CommandPermission("pickaria.pay")
internal class PayCommand(
    private val economyService: EconomyService,
    private val defaultCurrency: String,
    private val messager: Messager,
) : BaseCommand() {
    @Default
    @CommandCompletion("@players @amounts")
    fun onDefault(commandSender: CommandSender, onlinePlayer: OnlinePlayer, amount: Double) {
        if (amount <= 0.01) {
            messager.error(commandSender, "pay.invalid.amount")
            return
        }

        val price = Price.roundedToTwoDecimals(amount, defaultCurrency)
        val sender = commandSender as Player
        val recipient = onlinePlayer.player as Player

        try {
            economyService.createTransaction(sender.uniqueId, recipient.uniqueId, price).perform()
        } catch (_: BalanceInsufficientException) {
            messager.error(commandSender, "pay.balance.insufficient")
            return
        } catch (_: SenderCannotBeRecipientException) {
            messager.error(commandSender, "pay.cannot.self")
            return
        }

        val formattedPrice = economyService.format(price)
        messager.info(sender, "pay.sent", Text(recipient.name()), Text(formattedPrice))
        messager.info(recipient, "pay.received", Text(sender.name()), Text(formattedPrice))
    }
}
