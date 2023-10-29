package fr.pickaria.emerald

import co.aikar.commands.BukkitCommandManager
import fr.pickaria.emerald.controller.MoneyCommand
import fr.pickaria.emerald.controller.PayCommand
import fr.pickaria.emerald.data.EconomyConfig
import fr.pickaria.emerald.data.H2EconomyRepository
import fr.pickaria.emerald.domain.PaperEconomyService
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.decodeFromNativeReader
import org.bukkit.plugin.java.JavaPlugin


internal open class Main : JavaPlugin() {
    override fun onEnable() {
        val config = Toml.decodeFromNativeReader<EconomyConfig>(getResourceReader(this, "config.toml"))

        val economyRepository = H2EconomyRepository(config)
        val economyService = PaperEconomyService(economyRepository)
        openTestDatabase()

        val manager = BukkitCommandManager(this)
        manager.registerCommand(MoneyCommand(manager, economyService))
        manager.registerCommand(PayCommand(economyService))
    }
}
