package chaperone

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.io.File

data class AppConfig(
    val outputs: Outputs
)

data class Outputs(
    val stdout: StdOutOutputConfig? = null,
    val influxdb: InfluxDbOutputConfig? = null
)

sealed class OutputConfig

object StdOutOutputConfig : OutputConfig()

class InfluxDbOutputConfig(
    val defaultTags: Map<String, String>? = null,
    val db: String,
    val uri: String
) : OutputConfig()


fun loadConfig(configFile: File): AppConfig {
    check(configFile.exists()) { "Error: configured configFile wasn't found. [${configFile.path}]" }

    return Config()
        .from.toml.file(configFile)
        .toValue()
}