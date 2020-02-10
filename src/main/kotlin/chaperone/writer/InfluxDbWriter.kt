package chaperone.writer

import chaperone.Check
import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.OutputConfig
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import java.util.concurrent.TimeUnit

// influx config docs: https://micrometer.io/docs/registry/influx

@Suppress("unused")
class InfluxDbWriter : OutputWriter {

    private lateinit var meterRegistry: MeterRegistry

    override val typeName: String = "influxdb"

    override fun initialize(outputConfig: OutputConfig) {
        checkNotNull(outputConfig.db)
        checkNotNull(outputConfig.uri)

        val influxConfig: InfluxConfig = object : InfluxConfig {
            override fun get(k: String): String? = null

            override fun db(): String {
                return outputConfig.db
            }

            override fun uri(): String = outputConfig.uri
        }

        meterRegistry = InfluxMeterRegistry(influxConfig, Clock.SYSTEM)
        if (outputConfig.defaultTags != null) {
            meterRegistry.config().commonTags(outputConfig.defaultTags.map {
                Tag.of(it.key, it.value)
            }.toMutableList())
        }
    }

    override fun write(check: Check, checkResult: CheckResult) {
        // counts make more sense, but i couldn't figure out how to combine them with the statusmap plugin, since it wants
        // the retrieved value to be a discrete value of e.g. 0 for success, 1 for fail, etc.
        // https://grafana.com/grafana/plugins/flant-statusmap-panel

        // record a `0` for OK, and `1` for FAIL. then the influx query to find failures is using the max(upper) column.
        // sample query: SELECT max("upper") FROM "check_status_code" WHERE ("app" = 'foo') AND $timeFilter GROUP BY time($__interval), "check" fill(null)
        // this also allows us to alert on no data, since rows with 0 for values is different than no data.

        val value = if (checkResult.status == CheckStatus.OK) 0L else 1L
        meterRegistry.timer(
            "check.status.code",
            listOf(Tag.of("check", check.name))
        ).record(value, TimeUnit.MILLISECONDS)
    }
}