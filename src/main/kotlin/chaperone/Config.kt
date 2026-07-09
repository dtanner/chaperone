package chaperone

import chaperone.writer.OutputFormat
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.sources.EnvironmentVariablesPropertySource
import java.io.File

data class AppConfig(
    val apiServerPort: Int = 8080,
    val outputs: Outputs
)

data class Outputs(
    val log: LogWriterConfig? = null,
    val influxdb: InfluxDbWriterConfig? = null,
    val slack: SlackWriterConfig? = null,
    val command: CommandWriterConfig? = null,
)

data class LogWriterConfig(
    val destination: String = "stdout",
    val format: OutputFormat = OutputFormat.pretty,
    val onlyWriteFailures: Boolean = false
)

data class InfluxDbWriterConfig(
    val defaultTags: Map<String, String>? = null,
    val db: String,
    val uri: String,
    val onlyWriteFailures: Boolean = false
) {
    override fun toString(): String {
        return "db: $db, uri: $uri, defaultTags: $defaultTags"
    }
}

data class SlackWriterConfig(
    val webhook: String,
    val onlyWriteFailures: Boolean = false
)

data class CommandWriterConfig(
    val workingDirectory: String = ".",
    val command: String,
    val onlyWriteFailures: Boolean = false,
)

private const val ENV_PREFIX = "CHAPERONE_"

// pre-hoplite versions supported these two overrides with single-underscore separators. keep them working.
private val legacyEnvAliases = mapOf(
    "CHAPERONE_OUTPUTS_INFLUXDB_DB" to "CHAPERONE_OUTPUTS__INFLUXDB__DB",
    "CHAPERONE_OUTPUTS_INFLUXDB_URI" to "CHAPERONE_OUTPUTS__INFLUXDB__URI",
)

/**
 * Loads the app config from the given TOML file.
 * Any value can be overridden with a CHAPERONE_-prefixed environment variable, where a double
 * underscore separates path segments and a single underscore separates words within a camelCase field.
 * e.g. CHAPERONE_OUTPUTS__INFLUXDB__DB overrides outputs.influxdb.db,
 * and CHAPERONE_API_SERVER_PORT overrides apiServerPort.
 */
fun loadConfig(configFile: File, env: Map<String, String> = System.getenv()): AppConfig {
    check(configFile.exists()) { "Error: configured configFile wasn't found. [${configFile.path}]" }

    return ConfigLoaderBuilder.default()
        .addPropertySource(
            EnvironmentVariablesPropertySource(
                useUnderscoresAsSeparator = true,
                allowUppercaseNames = true,
                environmentVariableMap = {
                    env.filterKeys { it.startsWith(ENV_PREFIX) }
                        .mapKeys { legacyEnvAliases[it.key] ?: it.key }
                },
                prefix = ENV_PREFIX
            )
        )
        .addFileSource(configFile)
        .build()
        .loadConfigOrThrow<AppConfig>()
}
