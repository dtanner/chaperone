package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.LogOutputConfig
import org.junit.jupiter.api.Test


class LogWriterTest {
    private val okResult = CheckResult(
        name = "check",
        status = CheckStatus.OK,
        tags = mapOf("env" to "dev"),
        output = "all is ok"
    )

    @Test
    fun `pretty stdout`() {
        val logWriter = LogWriter(LogOutputConfig(destination = "stdout", format = OutputFormat.pretty))
        logWriter.write(okResult)
    }

    @Test
    fun `logstash file`() {
        val logWriter = LogWriter(LogOutputConfig(destination = "/tmp/logwriter.log", format = OutputFormat.logstash))
        logWriter.write(okResult)
    }
}