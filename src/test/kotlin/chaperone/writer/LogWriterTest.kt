package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.LogWriterConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldBeEmpty
import io.kotest.matchers.file.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File


class LogWriterTest {
    private val okResult = CheckResult(
        name = "check",
        status = CheckStatus.OK,
        tags = mapOf("env" to "dev"),
        output = "all is ok"
    )

    @Test
    fun `pretty stdout`() {
        val logWriter = LogWriter(LogWriterConfig(destination = "stdout", format = OutputFormat.pretty))
        // this is a weak test. so far just used for visually examining the output when the test is run
        logWriter.write(okResult)
    }

    @Test
    fun `logstash file`() {
        val file = File("/tmp/logstashfile.log")
        file.delete()
        val logWriter = LogWriter(LogWriterConfig(destination = file.absolutePath, format = OutputFormat.logstash))
        logWriter.write(okResult)
        file.shouldBeAFile()
        val line = file.readText()
        val jsonMap = jacksonObjectMapper().readValue<Map<String, String>>(line)
        jsonMap["message"].shouldBe(okResult.output)
        jsonMap["logger"].shouldBe("logwriter")
        jsonMap["level"].shouldBe("INFO")
        jsonMap["name"].shouldBe("check")
        jsonMap["tags"].shouldBe("{env=dev}")
        jsonMap["status"].shouldBe("OK")
    }

    @Test
    fun `only write errors`() {
        val file = File("/tmp/onlywriteerrors.log")
        file.delete()
        val logWriter = LogWriter(LogWriterConfig(
            destination = file.absolutePath,
            format = OutputFormat.logstash,
            onlyWriteFailures = true
        ))
        logWriter.write(okResult)
        file.shouldBeEmpty()

        logWriter.write(okResult.copy(status = CheckStatus.FAIL))
        file.shouldNotBeEmpty()
    }
}
