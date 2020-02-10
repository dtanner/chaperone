package chaperone

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.io.File

val jackson: ObjectMapper = ObjectMapper()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_NULL))
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .registerModule(Jdk8Module())
    .registerModule(JavaTimeModule())
    .registerModule(KotlinModule())

data class FooConfig(
    val outputs: List<GenericOutputConfig>
)

@JsonDeserialize(using = OutputConfigDeserializer::class)
sealed class GenericOutputConfig

object StdOutOutputConfig : GenericOutputConfig()

class InfluxDbOutputConfig(
    val defaultTags: Map<String, String>? = null,
    val db: String? = null,
    val uri: String? = null
) : GenericOutputConfig()

class OutputConfigDeserializer : StdDeserializer<GenericOutputConfig>(GenericOutputConfig::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GenericOutputConfig {
        val node: JsonNode = p.codec.readTree(p)
        return when (node.get("type").asText()) {
            "stdout" -> StdOutOutputConfig
            "influxdb" -> {
                InfluxDbOutputConfig(
                    db = node.get("db").asText(),
                    defaultTags = jackson.convertValue(node.get("defaultTags"), object : TypeReference<Map<String, String>>() {}),
                    uri = node.get("uri").asText()
                )
            }
            else -> throw Exception("unknown type")
        }

    }
}

// deserialization class for all possible output configuration values


fun loadFooConfig(configFile: File): FooConfig {
    return Config()
        .from.toml.file(configFile)
        .toValue()
}

