package chaperone.writer

import chaperone.Check
import chaperone.CheckResult
import chaperone.OutputConfig
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

@Suppress("unused")
class StdOutWriter : OutputWriter {
    override val typeName: String = "stdout"

    override fun initialize(outputConfig: OutputConfig) {} // nothing needed for initialization

    override fun write(check: Check, checkResult: CheckResult) {
        val formattedString = "${ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)} check: ${check.name.padEnd(15)} status: ${checkResult.status.name.padEnd(8)} output: ${checkResult.output}"
        log.debug { formattedString }
        println(formattedString)
    }
}