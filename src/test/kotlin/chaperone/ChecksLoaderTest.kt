package chaperone

import io.kotlintest.matchers.collections.shouldContain
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
        checks.shouldContain(
            Check(
                name = "sample",
                description = "sample check",
                command = "true",
                interval = Duration.ofMinutes(1),
                timeout = Duration.ofSeconds(30)
            )
        )
    }
}
