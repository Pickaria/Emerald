package fr.pickaria.emerald

import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.controller.*
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.H2EconomyRepository
import fr.pickaria.emerald.domain.PaperEconomyService
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import fr.pickaria.shared.getResourceReader
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin


internal open class Main : JavaPlugin() {
    override fun onEnable() {
        val config = Toml.decodeFromNativeReader<EconomyConfig>(getResourceReader(this, "config.toml"))

        val economyRepository: EconomyRepository = H2EconomyRepository(config)
        val economyService: EconomyService = PaperEconomyService(economyRepository)

        if (server.pluginManager.getPlugin("Vault") != null) {
            registerVaultCurrency(economyRepository, economyService)
        }

        server.servicesManager.register(EconomyService::class.java, economyService, this, ServicePriority.Normal)
        server.pluginManager.registerEvents(CollectValuableListener(economyService), this)

        val manager = BukkitCommandManager(this)
        manager.registerCommand(MoneyCommand(manager, economyService))
        manager.registerCommand(PayCommand(economyService))
        manager.registerCommand(EconomyCommand(economyService))
    }
}
