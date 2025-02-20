package rend

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object RendConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File("config/Rend/settings.json")

    var isEnabled: Boolean = false
    var leftClickDelay: Int = 5

    fun load() {
        configFile.parentFile?.mkdirs()

        if (configFile.exists()) {
            FileReader(configFile).use {
                val type = object : TypeToken<ConfigData>() {}.type
                val config = gson.fromJson<ConfigData>(it, type) ?: ConfigData()
                isEnabled = config.isEnabled
                leftClickDelay = config.leftClickDelay
                println("Loaded config: isEnabled=$isEnabled, leftClickDelay=$leftClickDelay")
            }
        } else {
            println("Config file not found, creating a new one.")
            save()
        }
    }

    fun save() {
        configFile.parentFile?.mkdirs()

        FileWriter(configFile).use {
            gson.toJson(ConfigData(isEnabled, leftClickDelay), it)
        }
    }

    data class ConfigData(
        val isEnabled: Boolean = false,
        val leftClickDelay: Int = 5
    )
}
