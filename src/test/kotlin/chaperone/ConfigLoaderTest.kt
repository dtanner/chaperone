package chaperone

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class ConfigLoaderTest {
    @Test
    fun `load toml config`() {
        val tomlSampleFile = File(javaClass.getResource("/test-config.toml").toURI())
        val config = loadConfig(tomlSampleFile)
        config.outputs.size.shouldBe(2)
        config.outputs.shouldContain(OutputConfig(type = "stdout"))
        config.outputs.shouldContain(
            OutputConfig(
                type = "influxdb",
                default_tags = mapOf("app" to "foo", "env" to "dev"),
                db = "test",
                uri = "http://localhost:8086"
            )
        )
    }
}
