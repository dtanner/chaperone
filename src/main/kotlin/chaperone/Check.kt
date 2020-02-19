package chaperone

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import mu.KotlinLogging
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

data class Check(
    val name: String,
    val description: String,
    val command: String,
    val interval: Duration,
    val timeout: Duration,
    val tags: Map<String, String> = mapOf()
) {
    fun execute(workingDirectory: File): CheckResult {
        log.debug { "$name: Executing $command" }
        return try {
            val bashCommand = arrayOf("/bin/bash", "-c", command)
            val proc = Runtime.getRuntime().exec(bashCommand, null, workingDirectory)
            proc.waitFor(timeout.seconds, TimeUnit.SECONDS)
            val output = proc.inputStream.bufferedReader().readText() + proc.errorStream.bufferedReader().readText()
            CheckResult(status = CheckStatus.fromExitCode(proc.exitValue()), output = output)
        } catch (e: Throwable) {
            log.error(e) { "Exception caught executing command: $this" }
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

fun loadChecks(checksDirectory: File): List<Check> {
    check(checksDirectory.isDirectory) { "Error: checksDirectory wasn't found. [${checksDirectory.path}]" }
    checksDirectory.listFiles() ?: throw IllegalStateException("checks directory is empty. ${checksDirectory.path}")

    val checks : MutableList<Check> = mutableListOf()
    checksDirectory.walkTopDown().forEach {
        if (it.isFile) {
            checks.add(Config().from.toml.file(it).toValue())
        }
    }
    return checks
}