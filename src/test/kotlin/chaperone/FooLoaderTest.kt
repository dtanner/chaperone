package chaperone

import org.junit.jupiter.api.Test
import java.io.File

class FooLoaderTest {
    @Test
    fun `load toml config`() {
        val tomlSampleFile = File(javaClass.getResource("/test-config.toml").toURI())
        val config = loadFooConfig(tomlSampleFile)
        println()
    }
}
