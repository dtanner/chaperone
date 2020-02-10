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
    // todo normalize the naming of cased words
    val default_tags: Map<String, String>? = null,
    val db: String? = null,
    val uri: String? = null
)

//enum class OutputConfigType(outputWriterImplementation: Class<out OutputWriter>) {
//    stdout(StdOutWriter::class.java),
//    influxdb(InfluxDbWriter::class.java)
//}

// todo figure out sealed class loading for konf, or conditionally non-null properties,
//  or a custom translator to go from big generic to output-specific config


// switch to json config format?
// allow users to choose?
fun loadConfig(configFile: File): AppConfig {
    return Config()
        .from.toml.file(configFile)
        .from.json.file(configFile)
        .toValue()
}