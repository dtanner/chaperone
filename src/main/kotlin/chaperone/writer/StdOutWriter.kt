package chaperone.writer

import chaperone.Check
import chaperone.CheckResult
import chaperone.OutputWriter
import mu.KotlinLogging

private val log = KotlinLogging.logger {}

class StdOutWriter : OutputWriter {
    override fun write(check: Check, checkResult: CheckResult) {
        val formattedString = "check: ${check.name.padEnd(15)} status: ${checkResult.status.name.padEnd(8)} output: ${checkResult.output}"
        log.debug { formattedString }
        println(formattedString)
    }
}