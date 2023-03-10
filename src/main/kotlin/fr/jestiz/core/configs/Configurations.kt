package fr.jestiz.core.configs

import fr.jestiz.core.Constants
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

    private fun messagePathNotFound(path: String, file: String): String {
        val pathIndex = Constants.CONFIGURATION_PATH_NOT_FOUND.indexOf("%path%");
        print(pathIndex)
        return Constants.CONFIGURATION_PATH_NOT_FOUND
            .replace(pathIndex, pathIndex + "%path%".length, path)
            .replace(Constants.CONFIGURATION_PATH_NOT_FOUND.indexOf("%file%"), Constants.CONFIGURATION_PATH_NOT_FOUND.indexOf("%file%") + "%file%".length, file).toString()
    }

    fun copyDefaultResource(fileName: String) {
        val file = File(Core.instance.dataFolder, fileName)
        if (file.exists())
            return

        file.parentFile.mkdirs()
        try {
            val inputStream = Core.instance.getResource(fileName) ?: return
            inputStream.use {
                file.outputStream().use { output -> it.copyTo(output) }
            }
        } catch (e: IOException) {
            Core.instance.logger.severe("Failed to copy resource $fileName")
        }
    }

/*    fun <T> getConfigMessage(message: String, vararg replacement: Pair<String, T>): String {
        val config = configs["messages.yml"] ?: return Constants.CONFIGURATION_NOT_FOUND
        if (!config.isSet(message))
            return messagePathNotFound(message, "messages.yml")

        lateinit var msg: String

        if (config.isList(message))
            config.getStringList(message)!!.forEach { msg += it + "\n"}
        else
            msg = config.getString(message)!!

        val builder = StringBuilder(msg)
        for ((key, value) in replacement)
            builder.replace(builder.indexOf(key), builder.indexOf(key) + key.length, value.toString())

        return builder.toString()
    }*/

    fun <T> getConfigMessage(message: String, vararg replacement: Pair<String, T>): String {
        val config = configs["messages.yml"] ?: return Constants.CONFIGURATION_NOT_FOUND

        if (!config.isSet(message))
            return messagePathNotFound(message, "messages.yml")

        val messageBuilder = StringBuilder()
        when {
            config.isList(message) -> config.getStringList(message)!!.forEach { messageBuilder.append(it).append("\n") }
            else -> messageBuilder.append(config.getString(message)!!)
        }

        for ((key, value) in replacement)
            messageBuilder.replace(messageBuilder.indexOf(key), messageBuilder.indexOf(key) + key.length, value.toString())

        return messageBuilder.toString()
    }

    fun getConfigMessage(message: String): String {
        return getConfigMessage(message, "" to "")
    }

}