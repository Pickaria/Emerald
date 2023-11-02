package fr.pickaria.emerald.controller

import co.aikar.commands.BaseCommand
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Currencies
import fr.pickaria.emerald.domain.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.shared.MiniMessage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("pay")
@CommandPermission("pickaria.command.pay")
class PayCommand(private val economyService: EconomyService) : BaseCommand() {
    @Default
    @CommandCompletion("@players")
    fun onDefault(player: CommandSender, onlinePlayer: OnlinePlayer, amount: Double) {
        if (amount <= 0.01) {
            throw InvalidCommandArgument("Le montant doit être supérieur à 0.01.")
        }

        val sender = player as Player
        val recipient = onlinePlayer.player as Player

        if (sender === recipient) {
            throw InvalidCommandArgument("Tu ne peux pas envoyer de l'argent à toi-même.")
        }

        try {
            val price = Price(amount, Currencies.CREDITS)
            economyService.withdraw(sender, price)
            economyService.deposit(recipient, price)

            val formattedPrice = economyService.format(price)

            MiniMessage("<gold><recipient><gray> a bien reçu <gold><amount><gray>.") {
                "recipient" to recipient.displayName()
                "amount" to formattedPrice
            }.send(sender)

            MiniMessage("<gold><sender><gray> vous a envoyé <gold><amount><gray>.") {
                "sender" to sender.displayName()
                "amount" to formattedPrice
            }.send(recipient)
        } catch (e: BalanceInsufficientException) {
            throw InvalidCommandArgument("Tu n'as pas assez d'argent.")
        }
    }
}