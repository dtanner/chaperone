package chaperone.writer

import chaperone.Check
import chaperone.CheckResult
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class StdOutWriter : OutputWriter {

    override fun write(check: Check, checkResult: CheckResult) {

        val sb = StringBuilder()
            .append(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).padEnd(33))
            .append(check.name.padEnd(20))
            .append(checkResult.status.name.padEnd(5))
            .append(checkResult.output?.trimEnd())

        println(sb)
    }
}