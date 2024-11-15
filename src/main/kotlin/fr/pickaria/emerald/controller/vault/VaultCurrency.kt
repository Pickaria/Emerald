package fr.pickaria.emerald.controller.vault

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.Unsafe
import fr.pickaria.emerald.domain.BalanceInsufficientException
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer

@OptIn(Unsafe::class)
internal class VaultCurrency(
    economyRepository: EconomyRepository,
    private val economyService: EconomyService,
    private val currencyName: String
) : AbstractEconomy() {
    private val config = economyRepository.getConfig(currencyName)

    // Constant methods
    override fun getName(): String = currencyName

    override fun fractionalDigits(): Int = 2

    override fun format(amount: Double): String = economyService.format(Price(amount, currencyName))

    override fun isEnabled(): Boolean = true

    override fun hasBankSupport(): Boolean = false

    override fun currencyNamePlural(): String = config.name.plural

    override fun currencyNameSingular(): String = config.name.singular

    // Logic methods
    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        return try {
            val account = economyService.getAccount(player)
            account.withdraw(Price(amount, currencyName))
            EconomyResponse(
                amount,
                account.getBalance(currencyName).amount,
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        } catch (_: BalanceInsufficientException) {
            val account = economyService.getAccount(player)
            EconomyResponse(
                amount,
                account.getBalance(currencyName).amount,
                EconomyResponse.ResponseType.FAILURE,
                "BalanceInsufficientException"
            )
        }
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        val account = economyService.getAccount(player)
        account.deposit(Price(amount, currencyName))
        return EconomyResponse(
            amount,
            account.getBalance(currencyName).amount,
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    override fun getBalance(player: OfflinePlayer): Double =
        economyService.getAccount(player).getBalance(currencyName).amount

    override fun has(player: OfflinePlayer, amount: Double): Boolean = getBalance(player) >= amount

    override fun hasAccount(player: OfflinePlayer): Boolean =
        economyService.getAvailableCurrencyNames().contains(currencyName)

    override fun createPlayerAccount(player: OfflinePlayer): Boolean = hasAccount(player)

    // Base methods
    @Deprecated("Deprecated in Java")
    override fun createBank(name: String?, player: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun deleteBank(name: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun bankBalance(name: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun bankHas(name: String?, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun bankWithdraw(name: String?, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun bankDeposit(name: String?, amount: Double): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun getBanks(): MutableList<String> = mutableListOf()

    @Deprecated("Deprecated in Java")
    override fun isBankOwner(name: String?, playerName: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    @Deprecated("Deprecated in Java")
    override fun isBankMember(name: String?, playerName: String?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    override fun isBankMember(name: String?, player: OfflinePlayer?): EconomyResponse =
        EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not support bank accounts!")

    // Deprecated methods
    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String): Boolean =
        economyService.getAvailableCurrencyNames().contains(currencyName)

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String): Double =
        economyService.getAccount(playerName).getBalance(currencyName).amount

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, amount: Double): Boolean = getBalance(playerName) >= amount

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse {
        return try {
            val account = economyService.getAccount(playerName)
            account.withdraw(Price(amount, currencyName))
            EconomyResponse(
                amount,
                account.getBalance(currencyName).amount,
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        } catch (_: BalanceInsufficientException) {
            val account = economyService.getAccount(playerName)
            EconomyResponse(
                amount,
                account.getBalance(currencyName).amount,
                EconomyResponse.ResponseType.FAILURE,
                "BalanceInsufficientException"
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse {
        val account = economyService.getAccount(playerName)
        account.deposit(Price(amount, currencyName))
        return EconomyResponse(
            amount,
            account.getBalance(currencyName).amount,
            EconomyResponse.ResponseType.SUCCESS,
            null
        )
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String): Boolean =
        hasAccount(playerName)

    // Using worlds

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String, worldName: String): Boolean {
        return hasAccount(playerName)
    }

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String, world: String): Double {
        return getBalance(playerName)
    }

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, worldName: String, amount: Double): Boolean {
        return has(playerName, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return withdrawPlayer(playerName, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String, worldName: String, amount: Double): EconomyResponse {
        return depositPlayer(playerName, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String, worldName: String): Boolean {
        return createPlayerAccount(playerName)
    }
}