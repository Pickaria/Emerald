package fr.pickaria.emerald.controller

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.Price
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import net.milkbowl.vault.economy.AbstractEconomy
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getOfflinePlayer
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin


private class Currency(
    private val economyRepository: EconomyRepository,
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

    override fun currencyNamePlural(): String = config.namePlural

    override fun currencyNameSingular(): String = config.nameSingular

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
        } catch (_: UnknownCurrencyException) {
            EconomyResponse(
                amount,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                null
            )
        }
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        return try {
            val account = economyService.getAccount(player)
            account.deposit(Price(amount, currencyName))
            EconomyResponse(
                amount,
                account.getBalance(currencyName).amount,
                EconomyResponse.ResponseType.SUCCESS,
                null
            )
        } catch (_: UnknownCurrencyException) {
            EconomyResponse(
                amount,
                0.0,
                EconomyResponse.ResponseType.FAILURE,
                null
            )
        }
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return try {
            economyService.getAccount(player).getBalance(currencyName).amount
        } catch (_: UnknownCurrencyException) {
            0.0
        }
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return try {
            getBalance(player) >= amount
        } catch (_: UnknownCurrencyException) {
            false
        }
    }

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
    override fun hasAccount(playerName: String): Boolean = hasAccount(getOfflinePlayer(playerName))

    @Deprecated("Deprecated in Java")
    override fun hasAccount(playerName: String?, worldName: String?): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String): Double = getBalance(getOfflinePlayer(playerName))

    @Deprecated("Deprecated in Java")
    override fun getBalance(playerName: String?, world: String?): Double {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String, amount: Double): Boolean = has(getOfflinePlayer(playerName), amount)

    @Deprecated("Deprecated in Java")
    override fun has(playerName: String?, worldName: String?, amount: Double): Boolean {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String, amount: Double): EconomyResponse =
        withdrawPlayer(getOfflinePlayer(playerName), amount)

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String, amount: Double): EconomyResponse =
        depositPlayer(getOfflinePlayer(playerName), amount)

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(playerName: String?, worldName: String?, amount: Double): EconomyResponse {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String): Boolean =
        createPlayerAccount(getOfflinePlayer(playerName))

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(playerName: String?, worldName: String?): Boolean {
        TODO("Not yet implemented")
    }
}

internal fun JavaPlugin.registerVaultCurrency(
    economyRepository: EconomyRepository,
    economyService: EconomyService,
) {
    val currencyKey = economyRepository.getDefaultCurrencyKey()
    try {
        val currency = Currency(economyRepository, economyService, currencyKey)
        Bukkit.getServicesManager().register(
            Economy::class.java,
            currency,
            this,
            ServicePriority.Normal,
        )
        Bukkit.getLogger().info("[Emerald] Registered Vault currency $currencyKey")
    } catch (e: UnknownCurrencyException) {
        Bukkit.getLogger().severe("[Emerald] Tried to register unknown ${e.currency} currency.")
    }
}
