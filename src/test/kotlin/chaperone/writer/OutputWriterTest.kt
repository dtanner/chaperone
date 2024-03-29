package chaperone.writer

import chaperone.loadConfig
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

internal class OutputWriterTest {
    @Test
    fun `wires up output writers`() {
        val configFile = File.createTempFile("chaperone-", ".toml").apply {
            writeText(
                """
                [outputs.log]
                format = "pretty"
                destination = "stdout"
                
                [outputs.influxdb]
                db="metrics"
                defaultTags={app="my-org-chaperone"}
                uri="http://localhost:8086"
                """.trimIndent()
            )
        }

        val appConfig = loadConfig(configFile)
        val outputWriters = initializeConfiguredOutputWriters(appConfig)

        outputWriters.filterIsInstance<LogWriter>().size.shouldBe(1)
        outputWriters.filterIsInstance<InfluxDbWriter>().size.shouldBe(1)
    }
}