package chaperone

import chaperone.writer.initializeConfiguredOutputWriters
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}

class App : CliktCommand() {
    private val checksDir: String by option(help = "directory containing the check definitions to run").default("/chaperone/checks.d")
    private val configFile: String by option(help = "configuration file").default("/chaperone/config.toml")

    override fun run() {
        log.info { "Starting Chaperone." }
        val checksDirFile = File(checksDir)
        val checks = loadChecks(checksDirFile)
        val config = loadConfig(File(configFile))
        val outputWriters = initializeConfiguredOutputWriters(config)

        runBlocking {
            val job = GlobalScope.launch {
                checks.forEach { check ->
                    launch {
                        while (true) {
                            val result = check.execute(checksDirFile)
                            outputWriters.forEach { it.write(check, result) }
                            delay(check.interval.toMillis())
                        }
                    }
                }
            }
            job.join()
        }

    }
}

fun main(args: Array<String>) = App().main(args)


