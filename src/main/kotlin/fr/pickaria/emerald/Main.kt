package fr.pickaria.emerald

import co.aikar.commands.BukkitCommandManager
import com.google.common.reflect.TypeToken
import fr.pickaria.emerald.controller.MoneyCommand
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.H2EconomyRepository
import fr.pickaria.emerald.domain.Currencies
import fr.pickaria.emerald.domain.EconomyService
import fr.pickaria.emerald.domain.PaperEconomyService
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File
import java.lang.reflect.ParameterizedType


internal open class Main : JavaPlugin {
    constructor() : super()
    protected constructor(
        loader: JavaPluginLoader?,
        description: PluginDescriptionFile?,
        dataFolder: File?,
        file: File?
    ) : super(
        loader!!, description!!, dataFolder!!, file!!
    )

    override fun onEnable() {
        logger.info("Hello, World!")

        val config = Toml.decodeFromNativeReader<EconomyConfig>(getResourceReader(this, "config.toml"))

        val economyRepository = H2EconomyRepository(config)
        openTestDatabase()

        val economyServiceType = object : TypeToken<EconomyService<Currencies>>() {}.type
        val economyServiceClass = (economyServiceType as ParameterizedType).rawType as Class<EconomyService<Currencies>>

        server.servicesManager.register(
            economyServiceClass,
            PaperEconomyService(economyRepository),
            this,
            ServicePriority.Normal
        )

        val economyService = server.servicesManager.getRegistration(economyServiceClass)!!.provider

        val manager = BukkitCommandManager(this)
        manager.registerCommand(MoneyCommand(manager, economyService))
    }
}
