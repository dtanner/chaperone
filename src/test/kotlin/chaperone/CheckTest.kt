package chaperone

import io.kotlintest.matchers.string.shouldBeBlank
import io.kotlintest.matchers.string.shouldNotBeBlank
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration

class CheckTest {
    @Test
    fun `success`() {
        val check = Check(
            name = "always succeeds",
            description = "should always be ok",
            command = "true",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(30)
        )

        val result = check.execute(File("."))
        result.status.shouldBe(CheckStatus.OK)
        result.output.shouldBeBlank()
    }

    @Test
    fun `success with output`() {
        val check = Check(
            name = "always succeeds",
            description = "should always be ok",
            command = "ls",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(30)
        )

        val result = check.execute(File("."))
        result.status.shouldBe(CheckStatus.OK)
        result.output.shouldNotBeBlank()
    }

    @Test
    fun `failure`() {
        val check = Check(
            name = "fail",
            description = "should fail",
            command = "false",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(30)
        )

        val result = check.execute(File("."))
        result.status.shouldBe(CheckStatus.FAIL)
        result.output.shouldBeBlank()
    }

    @Test
    fun `timeout`() {
        val check = Check(
            name = "timeout",
            description = "should timeout",
            command = "sleep 2",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(1)
        )

        val result = check.execute(File("."))
        result.status.shouldBe(CheckStatus.FAIL)
        result.output.shouldBeNull()
    }
}
