package chaperone.writer

import chaperone.CheckResult
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class StdOutWriter : OutputWriter {

    override fun write(checkResult: CheckResult) {

        val sb = StringBuilder()
            .append(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).padEnd(33))
            .append(checkResult.name.padEnd(30))
            .append(checkResult.status.name.padEnd(5))
            .append(checkResult.tags.toString().padEnd(25))

        checkResult.stdOut?.let { sb.append(it.trimEnd()) }
        checkResult.stdErr?.let { sb.append(" stderr: " + it.trimEnd()) }

        println(sb)
    }
}
