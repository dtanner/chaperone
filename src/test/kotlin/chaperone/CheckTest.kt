package chaperone

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration

class CheckTest {
    @Test
    fun `success no output`() {
        val check = Check(
            name = "always succeeds",
            description = "should always be ok",
            command = "true",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(30)
        )

        val results = check.execute(File("."))
        results[0].status.shouldBe(CheckStatus.OK)
        results[0].output.shouldBeNull()
    }

    @Test
    fun `success with output`() {
        val check = Check(
            name = "always succeeds",
            description = "should always be ok",
            command = "echo -n 'foo'",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(30)
        )

        val results = check.execute(File("."))
        results[0].status.shouldBe(CheckStatus.OK)
        results[0].output.shouldBe("foo")
    }

    @Test
    fun `should fail`() {
        val check = Check(
            name = "fail",
            description = "should fail",
            command = "false",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(30)
        )

        val results = check.execute(File("."))
        results[0].status.shouldBe(CheckStatus.FAIL)
        results[0].output.shouldBeNull()
    }

    @Test
    fun `should timeout`() {
        val check = Check(
            name = "timeout",
            description = "should timeout",
            command = "sleep 2",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(1)
        )

        val results = check.execute(File("."))
        results[0].status.shouldBe(CheckStatus.FAIL)
        results[0].output.shouldBeNull()
    }

    @Test
    fun `templated check single command arg example`() {

        val check = Check(
            name = "template check - $1",
            description = "template example",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(5),
            template = "for letter in a b; do echo \$letter; done",
            tags = mapOf("env" to "test", "letter" to "$1"),
            command = "echo -n $1"
        )

        val results = check.execute(File("."))

        results.size.shouldBe(2)
        results.all { it.status == CheckStatus.OK }.shouldBeTrue()

        val aOutput = results.find { it.name == "template check - a" }
        aOutput.shouldNotBeNull()
        aOutput.output?.shouldBe("a")
        aOutput.tags.shouldBe(mapOf("env" to "test", "letter" to "a"))

        val bOutput = results.find { it.name == "template check - b" }
        bOutput.shouldNotBeNull()
        bOutput.output?.shouldBe("b")
        bOutput.tags.shouldBe(mapOf("env" to "test", "letter" to "b"))
    }

    @Test
    fun `templated check multiple command arg example`() {

        val check = Check(
            name = "template - $2",
            description = "template example",
            interval = Duration.ofMinutes(1),
            timeout = Duration.ofSeconds(5),
            template = "for letter in a b; do echo x \$letter; done",
            tags = mapOf("env" to "test", "mode" to "$1", "letter" to "$2"),
            command = "echo -n $@"
        )

        val results = check.execute(File("."))

        results.size.shouldBe(2)
        results.all { it.status == CheckStatus.OK }.shouldBeTrue()

        val aOutput = results.find { it.name == "template - a" }
        aOutput.shouldNotBeNull()
        aOutput.output?.shouldBe("x a")
        aOutput.tags.shouldBe(mapOf("env" to "test", "mode" to "x", "letter" to "a"))

        val bOutput = results.find { it.name == "template - b" }
        bOutput.shouldNotBeNull()
        bOutput.output?.shouldBe("x b")
        bOutput.tags.shouldBe(mapOf("env" to "test", "mode" to "x",  "letter" to "b"))
    }

    @Test
    fun `bash string variable replacement`() {
        val command = "echo -n arg 0: $0, arg 1: \$1"
        val args = listOf("x", "y")
        val result = executeCommand(command = command, args = args)
        result.output.shouldBe("arg 0: x, arg 1: y")
    }

    @Test
    fun `command execution error should return the output`() {
        val command = "abc123"
        val result = executeCommand(command = command)
        result.status.shouldBe(CheckStatus.FAIL)
        result.output.shouldBe("/bin/bash: abc123: command not found\n")
    }

}
