package chaperone

import chaperone.writer.initializeConfiguredOutputWriters
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File


class App : CliktCommand() {
    private val checksDir: String by option(help = "directory containing the check definitions to run").default("/chaperone/checks.d")
    private val configFile: String by option(help = "configuration file").default("/chaperone/config.toml")

    override fun run() {
        val checks = loadChecks(File(checksDir))
        val config = loadConfig(File(configFile))
        val outputWriters = initializeConfiguredOutputWriters(config)

        runBlocking {
            val job = GlobalScope.launch {
                checks.forEach { check ->
                    launch {
                        while (true) {
                            val result = check.execute()
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


