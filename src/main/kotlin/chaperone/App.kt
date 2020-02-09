package chaperone

import chaperone.writer.StdOutWriter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.time.Duration

private val log = KotlinLogging.logger {}

class App : CliktCommand() {
    private val checksDir: String by option(help = "directory containing the check definitions to run").default("/chaperone/checks.d")
    private val configFile: String by option(help = "configuration file").default("/chaperone/config.toml")

    override fun run() {
        val checks = parseChecks(File(checksDir))
        val config = loadConfig(File(configFile))
        val outputWriters = buildOutputWriters()

        runBlocking {
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
    }
}

fun main(args: Array<String>) = App().main(args)

fun parseChecks(checksDir: File): List<Check> {
    // todo get config

    return listOf(
        Check(
            name = "alwaysworks",
            description = "will always work",
            command = "ls -lt",
            interval = Duration.ofSeconds(10),
            timeout = Duration.ofSeconds(5)
        ),
        Check(
            name = "alwaysfails",
            description = "always fail",
            command = "false",
            interval = Duration.ofSeconds(50),
            timeout = Duration.ofSeconds(10)
        )
    )
}

fun buildOutputWriters(): List<OutputWriter> {
    // todo use a config arg, or maybe parse the config here?
    return listOf(StdOutWriter())

    /*
publish result to the configured sink. e.g. for influx it'd be like "app=chaperone,check="happy",result=OK
 */


    /* pluggable configuration ideas:

    result_output_destinations [
      {
        type: stdout
      },
      {
        type:
      }
      {
        type: influxdb
        uri: http://foo
        tags: [
          { app: foo, env: dev }
        ]
        password_env_var_name - add later
      }
    ]

    influx config docs: https://micrometer.io/docs/registry/influx

    use https://grafana.com/grafana/plugins/flant-statusmap-panel for visualization
     */
}

