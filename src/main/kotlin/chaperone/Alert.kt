package chaperone

import mu.KotlinLogging
import java.io.File
import java.util.concurrent.TimeUnit

private val log = KotlinLogging.logger {}

fun loadAlertHandlers(alertsDirectory: File): List<File> {
    if (!alertsDirectory.exists()) return emptyList()

    check(alertsDirectory.isDirectory) { "Error: if alertsDirectory is defined, it must be a directory. [${alertsDirectory.path}]" }

    return alertsDirectory.listFiles()?.toList() ?: emptyList()
}

fun File.handleAlert(check: Check, checkResult: CheckResult, alertsDirectory: File) {
    try {
        val bashCommand = arrayOf("/bin/bash", "-c", this.absolutePath, check.name, checkResult.output)
        val proc = Runtime.getRuntime().exec(bashCommand, null, alertsDirectory)
        proc.waitFor(30, TimeUnit.SECONDS)
    } catch (e: Exception) {
        log.error(e) { "Exception caught executing alert handler: ${this.name} for check ${check.name}" }
    }
}

/*
tests:
    - send alert
    - alert recovery
    - threshold of e.g. 2
    - don't send alert after initial alert

todos:
    - encapsulate alert handling bettter
    - support noisy alerting?

 */
