package fr.pickaria.emerald

import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.Charset

fun getResourceReader(plugin: JavaPlugin, filename: String, replace: Boolean = false): InputStreamReader {
    val customConfigFile = File(plugin.dataFolder, filename)
    if (!customConfigFile.exists()) {
        customConfigFile.parentFile.mkdirs()
    }
    plugin.saveResource(filename, replace)

    return customConfigFile.inputStream().reader(Charset.defaultCharset())
}
