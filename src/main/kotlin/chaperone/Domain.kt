package chaperone

import mu.KotlinLogging
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

data class Check(
    val name: String,
    val description: String,
    val command: String,
    val interval: Duration,
    val timeout: Duration
) {
    fun execute(): CheckResult {
        log.debug { "$name: Executing $command" }
//        val argsList = mutableListOf(command)
//        if (args != null) {
//            argsList.addAll(args.map { " $it" })
//        }
        return try {
            val proc = Runtime.getRuntime().exec(command)

//            val proc = ProcessBuilder(argsList)
//                .redirectOutput(ProcessBuilder.Redirect.PIPE)
//                .redirectError(ProcessBuilder.Redirect.PIPE)
//                .start()

            proc.waitFor(timeout.seconds, TimeUnit.SECONDS)
            CheckResult(status = CheckStatus.fromExitCode(proc.exitValue()), output = proc.inputStream.bufferedReader().readText())
        } catch (e: Exception) {
            log.info(e) { "Exception caught executing command: $this" }
            CheckResult(status = CheckStatus.FAIL)
        }
    }

}

enum class CheckStatus {
    OK, FAIL;

    companion object {
        fun fromExitCode(exitCode: Int): CheckStatus {
            return if (exitCode == 0) OK else FAIL
        }
    }
}

data class CheckResult(
    val status: CheckStatus,
    val output: String? = null
)

interface OutputWriter {
    fun write(check: Check, checkResult: CheckResult)
}

sealed class OutputWriterConfig {
    object StdOut : OutputWriterConfig()
    data class InfluxDb(val uri: String, val tags: Map<String, String>) : OutputWriterConfig()
}
