package chaperone

import chaperone.json.objectMapper
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import mu.KotlinLogging
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

typealias Tags = Map<String, String>

data class Check(
    val name: String,
    val debug: Boolean = false,
    val description: String? = null,
    val template: String? = null,
    val templateOutputSeparator: String = System.lineSeparator(),
    val command: String,
    val interval: Duration,
    val timeout: Duration,
    val tags: Tags = mapOf()
) {
    fun execute(workingDirectory: File): List<CheckResult> {
        log.debug { "$name: Executing $command" }
        return try {

            if (template != null) {
                check(name.contains("$")) { "When a template is used, name must also be templated." }

                val templateResult = executeCommand(
                    workingDirectory = workingDirectory,
                    command = template,
                    timeout = timeout
                )
                if (templateResult.status == CheckStatus.FAIL) {
                    val message = "Error executing template. stdout: ${templateResult.stdOut}, stderr: ${templateResult.stdErr}"
                    println(message)
                    log.error { message }
                    return listOf(
                        CheckResult(
                            name = name,
                            status = CheckStatus.FAIL,
                            stdOut = templateResult.stdOut,
                            stdErr = templateResult.stdErr
                        )
                    )
                }
                check(!templateResult.stdOut.isNullOrBlank()) { "Required output from template command is missing." }

                val commandArgs = templateResult.stdOut.trim().split(templateOutputSeparator)
                return commandArgs.map { args ->
                    executeCheck(
                        name = generateName(name, args),
                        workingDirectory = workingDirectory,
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
                        workingDirectory = workingDirectory,
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
        return result.stdOut ?: throw java.lang.IllegalStateException("expected a generated name but none was produced")
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
        check(!result.stdOut.isNullOrBlank())
        // the above will produce something like "env:test letter:a"

        return result.stdOut.split(" ").associate { Pair(it.substringBefore(":"), it.substringAfter(":")) }
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

    val commandResult = executeCommand(workingDirectory = workingDirectory, command = command, timeout = timeout, debug = debug)

    return CheckResult(
        name = name,
        tags = tags,
        status = commandResult.status,
        stdOut = commandResult.stdOut,
        stdErr = commandResult.stdErr
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

    val processBuilder = if (args != null) {
        ProcessBuilder("/bin/bash", bashFlag, command, *args.toTypedArray())
    } else {
        ProcessBuilder("/bin/bash", bashFlag, command)
    }

    processBuilder.redirectErrorStream() // todo is this still needed/useful?
    processBuilder.directory(workingDirectory)
    val process = processBuilder.start()
    process.waitFor(timeout.seconds, TimeUnit.SECONDS)

    val stdOut = if (process.inputStream.available() > 0) {
        process.inputStream.bufferedReader().use { it.readText() }
    } else null

    val stdErr = if (process.errorStream.available() > 0) {
        process.errorStream.bufferedReader().use { it.readText() }
    } else null

    return CommandResult(
        status = CheckStatus.fromExitCode(process.exitValue()),
        stdOut = stdOut,
        stdErr = stdErr
    )
}

data class CommandResult(
    val status: CheckStatus,
    val stdOut: String? = null,
    val stdErr: String? = null
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
    val stdOut: String? = null,
    val stdErr: String? = null
)

fun loadChecks(checksDirectory: File): List<Check> {
    check(checksDirectory.isDirectory) { "Error: checksDirectory wasn't found. [${checksDirectory.path}]" }
    checksDirectory.listFiles() ?: throw IllegalStateException("checks directory is empty. ${checksDirectory.path}")

    val checks: MutableList<Check> = mutableListOf()
    checksDirectory.walkTopDown().forEach {
        if (it.isFile) {
            checks.add(Config().from.toml.file(it).toValue())
        }
    }
    return checks
}
