package chaperone.writer

import chaperone.AppConfig
import chaperone.CheckResult
import chaperone.InfluxDbOutputConfig

interface OutputWriter {
    fun write(checkResult: CheckResult)
}

fun initializeConfiguredOutputWriters(appConfig: AppConfig): List<OutputWriter> {

    // maybe todo use https://github.com/ronmamo/reflections or some other technique to dynamically load writers
    // the problem with a generic list is that the configuration isn't as clean

    val outputWriters: MutableList<OutputWriter> = mutableListOf()

    appConfig.outputs.stdout?.let {
        outputWriters.add(StdOutWriter())
    }

    appConfig.outputs.log?.let {
        outputWriters.add(LogWriter())
    }

    appConfig.outputs.influxdb?.let { config: InfluxDbOutputConfig ->
        outputWriters.add(InfluxDbWriter(config))
    }

    return outputWriters
}
