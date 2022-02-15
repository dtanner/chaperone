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
}
