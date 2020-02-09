package chaperone

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.io.File

data class AppConfig(
    val outputs: List<OutputConfig>
)

data class OutputConfig(
   val type: String,
   val tags: Map<String, String>? = null
)


fun loadConfig(configFile: File): AppConfig {
    return Config()
        .from.toml.file(configFile)
        .toValue()
}