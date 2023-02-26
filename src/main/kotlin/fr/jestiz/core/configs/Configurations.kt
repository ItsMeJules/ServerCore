package fr.jestiz.core.configs

import fr.jestiz.core.Core
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException

object Configurations {

    val configs = HashMap<String, FileConfiguration>()

    init {
        loadConfigurations()
    }

    private fun loadConfigurations() {
        File(Core.instance.dataFolder.absolutePath).walk()
            .filter { it.isFile }
            .forEach { file -> configs[file.name] = YamlConfiguration.loadConfiguration(file) }
    }

    private fun copyDefaultResource(fileName: String) {
        val file = File(Core.instance.dataFolder, fileName)
        if (file.exists())
            return

        file.parentFile.mkdirs()
        try {
            val inputStream = Core.instance.getResource(fileName) ?: return
            inputStream.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        } catch (e: IOException) {
            Core.instance.logger.severe("Failed to copy resource $fileName")
        }
    }

    fun createResources() {
        copyDefaultResource("config.yml")
        copyDefaultResource("messages.yml")
        copyDefaultResource("redis.yml")
    }

}