package fr.pickaria.emerald

import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
	override fun onEnable() {
		logger.info("Hello, World!")
	}
}