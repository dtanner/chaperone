package chaperone

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.io.File

data class AppConfig(
    val outputs: List<OutputConfig>
)

// deserialization class for all possible output configuration values
data class OutputConfig(
    val type: String,

    // influx
    val defaultTags: Map<String, String>? = null,
    val db: String? = null,
    val uri: String? = null
)

fun loadConfig(configFile: File): AppConfig {
    return Config()
        .from.toml.file(configFile)
        .toValue()
}