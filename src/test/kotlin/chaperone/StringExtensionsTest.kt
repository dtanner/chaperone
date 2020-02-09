package chaperone

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import java.time.Duration


class StringExtensionsTest {
    @Test
    fun `parse duration`() {
        "1m".parseDuration().shouldBe(Duration.ofMinutes(1))
        "10s".parseDuration().shouldBe(Duration.ofSeconds(10))
    }

    @Test
    fun `invalid input`() {
        shouldThrow<IllegalArgumentException> {
            "1".parseDuration()
        }

        shouldThrow<UnsupportedOperationException> {
            "1n".parseDuration()
        }

    }
}