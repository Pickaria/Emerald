package fr.pickaria.emerald

import fr.pickaria.bookshelf.i18n.loadBundle
import fr.pickaria.bookshelf.messager.Messager
import fr.pickaria.emerald.controller.*
import fr.pickaria.emerald.controller.vault.registerVaultCurrency
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.EconomyRepositoryImpl
import fr.pickaria.emerald.domain.EconomyServiceImpl
import fr.pickaria.emerald.domain.repositories.EconomyRepository
import fr.pickaria.shared.getStreamReader
import fr.pickaria.shared.withPlugin
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


internal open class Main : JavaPlugin() {
    override fun onEnable() {
        /* Init */
        val config = Toml.decodeFromNativeReader<EconomyConfig>(getStreamReader("config.toml"))
        loadBundle(
            "pickaria:translations_emerald",
            Locale.ENGLISH,
            ResourceBundle.getBundle("emerald.Bundle", Locale.FRENCH),
            ResourceBundle.getBundle("emerald.Bundle", Locale.ENGLISH),
        )
        val messager = Messager()

        /* Repositories */
        val economyRepository: EconomyRepository = EconomyRepositoryImpl(config)

        /* Services */
        val economyService: EconomyService = EconomyServiceImpl(economyRepository = economyRepository, plugin = this)
        server.servicesManager.register(EconomyService::class.java, economyService, this, ServicePriority.Normal)

        withPlugin("Vault") {
            registerVaultCurrency(economyRepository, economyService)
        }

        /* Events */
        server.pluginManager.registerEvents(CollectValuableListener(economyService), this)
        server.pluginManager.registerEvents(NotifyListener(economyService), this)
        server.pluginManager.registerEvents(CreateAccountListener(economyService), this)

        /* Commands */
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS) {
            val commands = it.registrar()
            PayCommand(
                economyService = economyService,
                messager = messager
            ).register(commands)
            MoneyCommand(
                economyService = economyService,
                messager = messager
            ).register(commands)
            EconomyCommand(
                economyService = economyService,
                messager = messager
            ).register(commands)
            BalanceTopCommand(
                economyService = economyService,
                messager = messager
            ).register(commands)
        }
    }
}
