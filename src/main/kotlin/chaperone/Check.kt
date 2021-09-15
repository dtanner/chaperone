package chaperone

import chaperone.json.objectMapper
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import mu.KotlinLogging
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


private val log = KotlinLogging.logger {}
private val stdErrLogging = Slf4jStream.of(log).asInfo()
private val cronParser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))

typealias Tags = Map<String, String>

data class Check(
    val fileDirectory: File? = null, // set when the check is loaded at runtime
    val name: String,
    val debug: Boolean = false,
    val description: String? = null,
    val template: String? = null,
    val templateOutputSeparator: String = System.lineSeparator(),
    val command: String,
    val interval: Duration? = null,
    val schedule: String? = null,
    val timeout: Duration,
    val tags: Tags = mapOf()
) {
    init {
        check(interval != null || schedule != null) { "Either interval or schedule must be defined for check named $name" }
        check(!(interval != null && schedule != null)) { "You cannot define both interval and schedule for the same check named $name" }

        schedule?.apply {
            CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(this).validate()
        }
    }

    fun execute(): List<CheckResult> {
        return try {
            requireNotNull(fileDirectory)

            if (template != null) {
                check(name.contains("$")) { "When a template is used, name must also be templated." }
                log.debug { "Executing template command: $template" }

                val templateResult = executeCommand(
                    workingDirectory = fileDirectory,
                    command = template,
                    timeout = timeout
                )
                if (templateResult.status == CheckStatus.FAIL) {
                    val message = "Error executing template. output: ${templateResult.output}"
                    log.error { message }
                    return listOf(
                        CheckResult(
                            name = template,
                            status = CheckStatus.FAIL,
                            output = templateResult.output
                        )
                    )
                }
                check(!templateResult.output.isNullOrBlank()) { "Required output from template command is missing." }

                val commandArgs = templateResult.output.trim().split(templateOutputSeparator)
                return commandArgs.map { args ->
                    executeCheck(
                        name = generateName(name, args),
                        workingDirectory = fileDirectory,
                        command = "$command $args",
                        timeout = timeout,
                        tags = generateTags(tags, args),
                        debug = debug
                    )
                }
            } else {
                return listOf(
                    executeCheck(
                        name = name,
                        workingDirectory = fileDirectory,
                        command = command,
                        timeout = timeout,
                        tags = tags,
                        debug = debug
                    )
                )
            }

        } catch (e: Throwable) {
            log.error(e) { "Exception caught executing command: $this" }
            listOf(CheckResult(name = name, status = CheckStatus.FAIL))
        }
    }

    /**
     * Used for template checks to dynamically generate each name.
     * Generates a name based on the combination of the nameCommand and the args from each templateCommand output.
     */
    private fun generateName(nameCommand: String, argsString: String): String {
        // escape any $ strings so they're not escaped too early
        val escapedCommand = nameCommand.replace("$", "\$")
        // bash -c 'echo -n $' is zero-based since there's no script. we want everything to be one-based for consistent configuration
        val oneBasedArgs = listOf("bugfound") + argsString.split(" ") //
        val result = executeCommand(command = "echo -n $escapedCommand", args = oneBasedArgs, timeout = Duration.ofSeconds(1))
        return result.output ?: throw java.lang.IllegalStateException("expected a generated name but none was produced")
    }

    /**
     * Used for template checks to dynamically generate the tag
     */
    private fun generateTags(tags: Tags, argsString: String): Tags {
        val serializedString = objectMapper.writeValueAsString(tags)
        // bash -c 'echo -n $' is zero-based since there's no script. we want everything to be one-based for consistent configuration
        val oneBasedArgs = listOf("bugfound") + argsString.split(" ")
        val result = executeCommand(
            command = "echo -n $serializedString",
            args = oneBasedArgs,
            timeout = Duration.ofSeconds(1)
        )
        check(!result.output.isNullOrBlank())
        // the above will produce something like "env:test letter:a"

        return result.output.split(" ").associate { Pair(it.substringBefore(":"), it.substringAfter(":")) }
    }

    fun millisToNextScheduledExecution(): Long {
        check(schedule != null)
        val executionTime = ExecutionTime.forCron(cronParser.parse(schedule))
        // adding a second to prevent fast-running checks from executing multiple times for the same schedule point
        // if this is problematic, we can add some state and ensure we don't repeat check times using nextExecution()
        return executionTime.timeToNextExecution(ZonedDateTime.now()).get().plusSeconds(1).toMillis()
    }
}

/**
 * Wrapper around executeCommand, augmenting with the check metadata
 */
fun executeCheck(
    name: String,
    tags: Map<String, String> = mapOf(),
    workingDirectory: File,
    command: String,
    timeout: Duration,
    debug: Boolean
): CheckResult {

    log.debug { "$name: executing command: $command" }

    val commandResult = executeCommand(workingDirectory = workingDirectory, command = command, timeout = timeout, debug = debug)

    return CheckResult(
        name = name,
        tags = tags,
        status = commandResult.status,
        output = commandResult.output
    )
}

/**
 * Executes a bash command and returns a result
 */
fun executeCommand(
    workingDirectory: File = File("."),
    command: String,
    args: List<String>? = null,
    timeout: Duration = Duration.ofSeconds(5),
    debug: Boolean = false
): CommandResult {

    val bashFlag = if (debug) "-cx" else "-c"

    try {
        val processExecutor = ProcessExecutor()
        if (args != null) {
            processExecutor.command("/bin/bash", bashFlag, command, *args.toTypedArray())
        } else {
            processExecutor.command("/bin/bash", bashFlag, command)
        }
        val processResult = processExecutor
            .directory(workingDirectory)
            .readOutput(true)
            .redirectError(stdErrLogging)
            .timeout(timeout.seconds, TimeUnit.SECONDS)
            .execute()

        return CommandResult(
            status = CheckStatus.fromExitCode(processResult.exitValue),
            output = processResult.outputUTF8()
        )
    } catch (e: TimeoutException) {
        log.warn { "timeout occurred. command: [$command] args: [$args]" }
        return CommandResult(
            status = CheckStatus.FAIL,
            output = "timeout executing check"
        )
    }
}

data class CommandResult(
    val status: CheckStatus,
    val output: String? = null
)

enum class CheckStatus {
    OK, FAIL;

    companion object {
        fun fromExitCode(exitCode: Int): CheckStatus {
            return if (exitCode == 0) OK else FAIL
        }
    }
}

data class CheckResult(
    val name: String,
    val status: CheckStatus,
    val tags: Map<String, String> = mapOf(),
    val output: String? = null
)
