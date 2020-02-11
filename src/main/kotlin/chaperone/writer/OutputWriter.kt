package chaperone.writer

import chaperone.AppConfig
import chaperone.Check
import chaperone.CheckResult
import chaperone.InfluxDbOutputConfig

interface OutputWriter {
    fun write(check: Check, checkResult: CheckResult)
}


fun initializeConfiguredOutputWriters(appConfig: AppConfig): List<OutputWriter> {

    // maybe todo use https://github.com/ronmamo/reflections or some other technique to dynamically load writers
    // the problem with a generic list is that the configuration isn't as clean looking as it could be

    val outputWriters: MutableList<OutputWriter> = mutableListOf()

    appConfig.outputs.stdout?.let {
        val writer = StdOutWriter()
        outputWriters.add(writer)
    }

    appConfig.outputs.influxdb?.let { config: InfluxDbOutputConfig ->
        val writer = InfluxDbWriter(config)
        outputWriters.add(writer)
    }

    return outputWriters
}

