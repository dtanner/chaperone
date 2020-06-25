package chaperone.writer

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.LogOutputConfig
import net.logstash.logback.encoder.LogstashEncoder
import net.logstash.logback.fieldnames.LogstashFieldNames
import net.logstash.logback.marker.Markers.appendEntries
import net.logstash.logback.stacktrace.ShortenedThrowableConverter
import org.slf4j.LoggerFactory


@Suppress("EnumEntryName")
enum class OutputFormat {
    pretty,
    logstash
}

class LogWriter(private val config: LogOutputConfig) : OutputWriter {

    private val log: Logger = configureLogger(config)

    override fun write(checkResult: CheckResult) {
        if (config.onlyWriteFailures && checkResult.status != CheckStatus.FAIL) return

        checkResult.apply {
            val logMap = mapOf(
                "name" to name,
                "tags" to tags.toString(),
                "status" to status.name
            )

            when (status) {
                CheckStatus.OK -> log.info(appendEntries(logMap), output)
                CheckStatus.FAIL -> log.error(appendEntries(logMap), output)
            }
        }
    }

    private fun configureLogger(config: LogOutputConfig): Logger {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        val messageEncoder = when (config.format) {
            OutputFormat.pretty -> {
                PatternLayoutEncoder().apply {
                    pattern = "%date %level %marker %msg %n"
                    context = loggerContext
                    start()
                }
            }
            OutputFormat.logstash -> {
                LogstashEncoder().apply {
                    throwableConverter = ShortenedThrowableConverter().apply {
                        maxDepthPerThrowable = 30
                        maxLength = 2048
                        shortenedClassNameLength = 100
                    }
                    fieldNames = LogstashFieldNames().apply {
                        thread = null
                        version = null
                        levelValue = null
                        logger = "logger"
                    }
                    start()
                }
            }
        }

        val appender: Appender<ILoggingEvent> = when (config.destination) {
            "stdout" -> {
                val consoleAppender = ConsoleAppender<ILoggingEvent>().apply {
                    context = loggerContext
                    name = "console"
                    encoder = messageEncoder
                    start()
                }
                consoleAppender
            }
            // else assume the destination is a file path
            else -> {
                val logFileAppender = RollingFileAppender<ILoggingEvent>().apply {
                    context = loggerContext
                    name = "logFile"
                    encoder = messageEncoder
                    isAppend = true
                    file = config.destination

                }

                val logFilePolicy = TimeBasedRollingPolicy<ILoggingEvent>().apply {
                    context = loggerContext
                    setParent(logFileAppender)
                    compressionMode
                    fileNamePattern = "${config.destination}.%d{yyyy-MM-dd}.gz"
                    maxHistory = 3
                    start()
                }

                logFileAppender.apply {
                    rollingPolicy = logFilePolicy
                    start()
                }
            }
        }

        val logger: Logger = LoggerFactory.getLogger("logwriter") as Logger
        logger.apply {
            level = Level.INFO
            isAdditive = false
            addAppender(appender)
        }
        return logger
    }

}


