package chaperone.writer

import chaperone.*

interface OutputWriter {
    fun write(checkResult: CheckResult)
}

fun initializeConfiguredOutputWriters(appConfig: AppConfig): List<OutputWriter> {

    // maybe todo use https://github.com/ronmamo/reflections or some other technique to dynamically load writers
    // the problem with a generic list is that the configuration isn't as clean

    val outputWriters: MutableList<OutputWriter> = mutableListOf()

    appConfig.outputs.log?.let { config: LogWriterConfig ->
        outputWriters.add(LogWriter(config))
    }

    appConfig.outputs.influxdb?.let { config: InfluxDbWriterConfig ->
        outputWriters.add(InfluxDbWriter(config))
    }

    appConfig.outputs.slack?.let { config: SlackWriterConfig ->
        outputWriters.add(SlackWriter(config))
    }

    appConfig.outputs.command?.let { config: CommandWriterConfig ->
        outputWriters.add(CommandWriter(config))
    }

    return outputWriters
}
