package fr.pickaria.emerald

import fr.pickaria.acf.initCommandManager
import fr.pickaria.emerald.controller.*
import fr.pickaria.emerald.controller.vault.registerVaultCurrency
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.EconomyRepositoryImpl
import fr.pickaria.emerald.domain.PaperEconomyService
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import fr.pickaria.shared.getStreamReader
import fr.pickaria.shared.withPlugin
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin


internal open class Main : JavaPlugin() {
    override fun onEnable() {
        val config = Toml.decodeFromNativeReader<EconomyConfig>(getStreamReader("config.toml"))

        val economyRepository: EconomyRepository = EconomyRepositoryImpl(config)
        val economyService: EconomyService = PaperEconomyService(economyRepository = economyRepository, plugin = this)

        withPlugin("Vault") {
            registerVaultCurrency(economyRepository, economyService)
        }

        server.servicesManager.register(EconomyService::class.java, economyService, this, ServicePriority.Normal)
        server.pluginManager.registerEvents(CollectValuableListener(economyService), this)
        server.pluginManager.registerEvents(NotifyListener(economyService), this)

        val manager = initCommandManager()
        manager.registerCommand(MoneyCommand(manager, economyService))
        manager.registerCommand(PayCommand(economyService, economyRepository.getDefaultCurrencyKey()))
        manager.registerCommand(EconomyCommand(economyService, manager))
    }
}
