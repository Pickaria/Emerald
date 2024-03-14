package fr.pickaria.emerald.controller

import fr.pickaria.emerald.domain.UnknownCurrencyException
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

internal fun JavaPlugin.registerVaultCurrency(economyRepository: EconomyRepository) {
    if (server.pluginManager.getPlugin("Vault") != null) {
        val currencyName = economyRepository.getDefaultCurrencyName()
        try {
            val defaultCurrency = economyRepository.getConfig(currencyName)
//            Bukkit.getServicesManager().register(Economy::class.java, it.economy, this, ServicePriority.Normal)
            Bukkit.getLogger().info("Economy registered currency `${defaultCurrency.nameSingular}`.")
        } catch (e: UnknownCurrencyException) {
            Bukkit.getLogger().severe("Tried to register unknown `${e.currency}` currency.")
        }
    }
}
