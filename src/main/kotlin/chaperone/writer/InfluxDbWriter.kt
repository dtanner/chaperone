package chaperone.writer

import chaperone.Check
import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.InfluxDbOutputConfig
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

class InfluxDbWriter(config: InfluxDbOutputConfig) : OutputWriter {

    private val meterRegistry: MeterRegistry

    init {
        val influxConfig: InfluxConfig = object : InfluxConfig {
            override fun get(k: String): String? = null

            override fun db(): String {
                return config.db
            }

            override fun uri(): String = config.uri
        }

        log.info { "influxdb config: $config" }

        meterRegistry = InfluxMeterRegistry(influxConfig, Clock.SYSTEM)
        if (config.defaultTags != null) {
            meterRegistry.config().commonTags(config.defaultTags.toTagList())
        }
    }

    override fun write(check: Check, checkResult: CheckResult) {
        // counts make more logical sense, but i couldn't figure out how to combine them with the statusmap plugin, since it wants
        // the retrieved value to be a discrete value of e.g. 0 for success, 1 for fail, etc.
        // https://grafana.com/grafana/plugins/flant-statusmap-panel

        // record a `0` for OK, and `1` for FAIL. then use query to find failures using the max(upper) column.
        // sample query: SELECT max("upper") FROM "check_status_code" WHERE ("app" = 'foo') AND $timeFilter GROUP BY time($__interval), "check" fill(null)
        // this also allows us to alert on no data, since rows with 0 for values is different than no data.

        val value = if (checkResult.status == CheckStatus.OK) 0L else 1L
        val tags = check.tags.toTagList()
        tags.add(Tag.of("check", check.name))
        meterRegistry.timer("check.status.code", tags).record(value, TimeUnit.MILLISECONDS)
    }
}

fun Map<String, String>.toTagList(): MutableList<Tag> {
    return this.map { Tag.of(it.key, it.value) }.toMutableList()
}