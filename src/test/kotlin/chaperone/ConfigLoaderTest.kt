package chaperone

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class ConfigLoaderTest {

    @Test
    fun `load toml config`() {
        val tomlSampleFile = File(javaClass.getResource("/test-config.toml").toURI())
        val config = loadConfig(tomlSampleFile)

        config.outputs.log.shouldNotBeNull()

        val influxConfig = config.outputs.influxdb
        influxConfig.shouldNotBeNull()
        influxConfig.db.shouldBe("test")
        influxConfig.defaultTags.shouldBe(mapOf("app" to "foo"))
        influxConfig.uri.shouldBe("http://localhost:8086")
    }

    @Test
    fun `environment variables override toml config values`() {
        val tomlSampleFile = File(javaClass.getResource("/test-config.toml").toURI())
        val config = loadConfig(
            tomlSampleFile,
            env = mapOf(
                "CHAPERONE_OUTPUTS__INFLUXDB__DB" to "overridden-db",
                "CHAPERONE_OUTPUTS__INFLUXDB__ONLY_WRITE_FAILURES" to "true",
                "CHAPERONE_API_SERVER_PORT" to "9999"
            )
        )

        val influxConfig = config.outputs.influxdb
        influxConfig.shouldNotBeNull()
        influxConfig.db.shouldBe("overridden-db")
        influxConfig.onlyWriteFailures.shouldBe(true)
        config.apiServerPort.shouldBe(9999)
        // values not overridden still come from the toml file
        influxConfig.uri.shouldBe("http://localhost:8086")
        influxConfig.defaultTags.shouldBe(mapOf("app" to "foo"))
    }

    @Test
    fun `legacy single-underscore influxdb env overrides still work`() {
        val tomlSampleFile = File(javaClass.getResource("/test-config.toml").toURI())
        val config = loadConfig(
            tomlSampleFile,
            env = mapOf(
                "CHAPERONE_OUTPUTS_INFLUXDB_DB" to "legacy-db",
                "CHAPERONE_OUTPUTS_INFLUXDB_URI" to "http://influxdb:8086"
            )
        )

        val influxConfig = config.outputs.influxdb
        influxConfig.shouldNotBeNull()
        influxConfig.db.shouldBe("legacy-db")
        influxConfig.uri.shouldBe("http://influxdb:8086")
    }
}
