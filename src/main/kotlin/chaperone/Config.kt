package chaperone

import chaperone.writer.OutputFormat
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.io.File

data class AppConfig(
    val apiServerPort: Int = 8080,
    val outputs: Outputs
)

data class Outputs(
    val log: LogOutputConfig? = null,
    val influxdb: InfluxDbOutputConfig? = null,
    val slack: SlackOutputConfig? = null
)

sealed class OutputConfig

data class LogOutputConfig(
    val destination: String = "stdout",
    val format: OutputFormat = OutputFormat.pretty,
    val onlyWriteFailures: Boolean = false
) : OutputConfig()

data class InfluxDbOutputConfig(
    val defaultTags: Map<String, String>? = null,
    var db: String,
    var uri: String,
    val onlyWriteFailures: Boolean = false
) : OutputConfig() {
    override fun toString(): String {
        return "db: $db, uri: $uri, defaultTags: $defaultTags"
    }
}

data class SlackOutputConfig(
    val webhook: String,
    val onlyWriteFailures: Boolean = false
)

fun loadConfig(configFile: File): AppConfig {
    check(configFile.exists()) { "Error: configured configFile wasn't found. [${configFile.path}]" }

    val appConfig = Config()
        .from.toml.file(configFile)
        .toValue<AppConfig>()

    // todo figure out how to merge `.from.env` into this properly.
    // to fix ^, we need to not use camelCase naming or use the Config object. see https://github.com/uchuhimo/konf/issues/51
    val env = System.getenv()
    appConfig.outputs.influxdb?.let { influxDbOutputConfig ->
        env["CHAPERONE_OUTPUTS_INFLUXDB_DB"]?.let { influxDbOutputConfig.db = it }
        env["CHAPERONE_OUTPUTS_INFLUXDB_URI"]?.let { influxDbOutputConfig.uri = it }
    }

    return appConfig
}