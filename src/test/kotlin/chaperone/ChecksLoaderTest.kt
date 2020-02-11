package chaperone

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration

class ChecksLoaderTest {
    @Test
    fun `load toml checks`() {
        val checksDir = File(javaClass.getResource("/test-checks.d").toURI())
        val checks = loadChecks(checksDir)
        checks.size.shouldBe(1)
        checks.first().shouldBe(
            Check(
                name = "sample-dev",
                description = "sample dev check",
                command = "true",
                interval = Duration.ofSeconds(10),
                timeout = Duration.ofSeconds(30),
                tags = mapOf("env" to "dev")
            )
        )
    }
}
