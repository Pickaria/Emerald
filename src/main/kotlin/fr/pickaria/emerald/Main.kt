package fr.pickaria.emerald

import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.controller.MoneyCommand
import fr.pickaria.emerald.data.H2EconomyRepository
import fr.pickaria.emerald.domain.PaperEconomyService
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.JavaPluginLoader
import java.io.File


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
        val economyRepository = H2EconomyRepository()
        val economyService = PaperEconomyService(economyRepository)

        val manager = BukkitCommandManager(this)
        manager.registerCommand(MoneyCommand(manager, economyService))
    }
}
