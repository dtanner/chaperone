package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus
import mu.KotlinLogging

class LogWriter : OutputWriter {

    private val log = KotlinLogging.logger("chaperone")

    override fun write(checkResult: CheckResult) {
        checkResult.apply {
            val message = """
                name: $name
                tags: $tags
                output: $output
            """.trimIndent()

            when (status) {
                CheckStatus.OK -> log.info(message)
                CheckStatus.FAIL -> log.error(message)
            }
        }
    }
}