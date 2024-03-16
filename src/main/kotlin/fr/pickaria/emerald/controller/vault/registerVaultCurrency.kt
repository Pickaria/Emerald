package fr.pickaria.emerald.controller.vault

import fr.pickaria.emerald.EconomyService
import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin


internal fun JavaPlugin.registerVaultCurrency(
    economyRepository: EconomyRepository,
    economyService: EconomyService,
) {
    val currencyKey = economyRepository.getDefaultCurrencyKey()
    try {
        val currency = VaultCurrency(economyRepository, economyService, currencyKey)
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
