package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.InfluxDbOutputConfig
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import java.time.Instant

private val log = KotlinLogging.logger {}

class InfluxDbWriter(private val config: InfluxDbOutputConfig) : OutputWriter {

    private val client = OkHttp(OkHttpClient())

    init {
        log.info { "influxdb config: $config" }

        // create database if it doesn't already exist
        val request = Request(Method.POST, "${config.uri}/query").query("q", "CREATE DATABASE ${config.db}")
        client(request) { response: Response ->
            if (response.status.code != 200) {
                log.error { "Unexpected status ensuring influxdb exists. code=${response.status.code} message=${response.bodyString()}" }
            }
        }
    }

    override fun write(checkResult: CheckResult) {
        val tags = (config.defaultTags ?: emptyMap()).plus(checkResult.tags).plus(Pair("check", checkResult.name)).toSortedMap()

        // maybe todo batch write?

        val line = generateLine(checkResult, tags, Instant.now().toEpochMilli())

        val request = Request(Method.POST, "${config.uri}/write")
            .query("db", config.db)
            .query("precision", "ms")
            .body(line)

        client(request) { response: Response ->
            if (response.status.code != 204) {
                log.error { "Unexpected status returned posting to influxdb. code=${response.status.code} message=${response.bodyString()}" }
            }
        }

    }

    fun generateLine(checkResult: CheckResult, tags: Map<String, String>, timestamp: Long): String {
        // https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_tutorial/#special-characters
        val tagString = tags.map { "${it.key.replace(" ", "\\ ")}=${it.value.replace(" ", "\\ ")}" }.joinToString(separator = ",")

        val fields = "value=${if (checkResult.status == CheckStatus.OK) "0i" else "1i"}"

        return "check_status_code,$tagString $fields $timestamp"
    }
}
