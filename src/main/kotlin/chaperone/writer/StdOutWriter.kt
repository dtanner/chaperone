package chaperone.writer

import chaperone.Check
import chaperone.CheckResult
import mu.KotlinLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val log = KotlinLogging.logger {}

class StdOutWriter : OutputWriter {

    override fun write(check: Check, checkResult: CheckResult) {
        val formattedString = "${ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)} " +
                "check: ${check.name.padEnd(12)} " +
                "tags: ${check.tags.toString().padEnd(10)} " +
                "status: ${checkResult.status.name.padEnd(5)} " +
                "output: ${checkResult.output}"
        log.debug { formattedString }
        println(formattedString)
    }
}