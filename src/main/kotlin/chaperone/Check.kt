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
    val timeout: Duration
) {
    fun execute(): CheckResult {
        log.debug { "$name: Executing $command" }
        return try {
            val bashCommand = arrayOf("/bin/bash", "-c", command)
            val proc = Runtime.getRuntime().exec(bashCommand)
            proc.waitFor(timeout.seconds, TimeUnit.SECONDS)
            CheckResult(status = CheckStatus.fromExitCode(proc.exitValue()), output = proc.inputStream.bufferedReader().readText())
        } catch (e: Exception) {
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
    check(checksDirectory.isDirectory) { "Error: configured checksDirectory wasn't found. [${checksDirectory.path}]" }
    val checksFiles = checksDirectory.listFiles()
    if (checksFiles == null || checksFiles.isEmpty()) {
        throw IllegalStateException("checks directory is empty: ${checksDirectory.name}")
    }

    return checksFiles.map { checksFile ->
        Config()
            .from.toml.file(checksFile)
            .toValue<Check>()
    }

}