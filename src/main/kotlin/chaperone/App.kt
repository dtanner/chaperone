package chaperone

import chaperone.json.objectMapper
import chaperone.writer.initializeConfiguredOutputWriters
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.string
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
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

        startApiServer(checks, config.apiServerPort)

        runBlocking {
            val job = launch(Dispatchers.IO) {
                checks.forEach { check ->
                    launch {
                        while (true) {
                            // for scheduled checks, delay before each execution
                            check.schedule?.run {
                                delay(check.millisToNextScheduledExecution())
                            }

                            check.execute(outputWriters)

                            // for interval checks, delay after each execution
                            check.interval?.run {
                                delay(this.toMillis())
                            }
                        }
                    }
                }
            }
            job.join()
        }
    }

    private fun startApiServer(checks: List<Check>, port: Int) {
        val routingHttpHandler = routes(
            "health" bind Method.GET to {
                Response(OK)
                    .header("Content-Type", ContentType.TEXT_PLAIN.toHeaderValue())
                    .body("healthy")
            },
            "checks" bind Method.GET to {
                Response(OK).with(
                    Body.string(ContentType.APPLICATION_JSON).toLens().of(
                        objectMapper.writeValueAsString(checks)
                    )
                )
            }
        )

        routingHttpHandler.asServer(Undertow(port)).start()
    }
}

fun main(args: Array<String>) = App().main(args)
