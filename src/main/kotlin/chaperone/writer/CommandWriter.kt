package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus.FAIL
import chaperone.CheckStatus.OK
import chaperone.CommandWriterConfig
import chaperone.executeCommand
import chaperone.json.objectMapper
import mu.KotlinLogging
import java.io.File



/**
 * Writes check output to a command that you specify.
 * The CheckResult will be passed as stdin in JSON format to the command.
 */
class CommandWriter(private val config: CommandWriterConfig) : OutputWriter {

    private val log = KotlinLogging.logger {}

    override fun write(checkResult: CheckResult) {
        if (config.onlyWriteFailures && checkResult.status != FAIL) return

        val checkResultJson = objectMapper.writeValueAsString(checkResult)

        // reusing the executeCommand function from Check. we have to squint a little bit but seems ok so far.
        val result = executeCommand(
            workingDirectory = File(config.workingDirectory),
            command = config.command,
            args = listOf("'$checkResultJson'"),
        )
        log.debug { "Command ${config.command} with args $checkResultJson called for check ${checkResult.name}" }
        if (result.status != OK) {
            log.error { "Error writing check result to command: ${result.output}" }
        }
    }

}

