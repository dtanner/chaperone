package chaperone.writer

import chaperone.AppConfig
import chaperone.Check
import chaperone.CheckResult
import chaperone.OutputConfig

interface OutputWriter {
    val typeName: String
    fun initialize(outputConfig: OutputConfig)
    fun write(check: Check, checkResult: CheckResult)
}


fun initializeConfiguredOutputWriters(appConfig: AppConfig): List<OutputWriter> {
    // todo use https://github.com/ronmamo/reflections or some other technique to dynamically load writers
    return appConfig.outputs.map { outputConfig: OutputConfig ->
        val outputWriter: OutputWriter = when (outputConfig.type) {
            "stdout" -> StdOutWriter()
            "influxdb" -> InfluxDbWriter()
            else -> throw IllegalArgumentException("Unknown config type: ${outputConfig.type}")
        }
        outputWriter.initialize(outputConfig)
        outputWriter
    }
}

