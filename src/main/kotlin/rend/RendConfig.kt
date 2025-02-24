package rend

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

object RendConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile = File("config/Rend/settings.json")

    var config: ConfigData = ConfigData()

    fun load() {
        configFile.parentFile?.mkdirs()

        config = if (configFile.exists()) {
            configFile.reader().use {
                gson.fromJson(it, object : TypeToken<ConfigData>() {}.type) ?: ConfigData()
            }
        } else {
            println("Config file not found, creating a new one.")
            save()
            ConfigData()
        }
        println("Loaded config: $config")
    }

    fun save() {
        configFile.parentFile?.mkdirs()

        configFile.writer().use {
            gson.toJson(config, it)
        }
    }

    data class ConfigData(
        var isEnabled: Boolean = false,
        var leftClickDelay: Int = 5,
        var autoFaceKuudra: Boolean = false,
        var eqDelay: Int = 3
    )
}
