package chaperone

import java.time.Duration

/**
 * For a given String expected to be like 10m, or 5s, returns a Duration representing the value and unit of measure.
 */
fun String.parseDuration(): Duration {
    val value = this.take(this.length - 1)
    require(!value.isBlank() && value.all { it.isDigit() })

    return when (this.takeLast(1)) {
        "s" -> Duration.ofSeconds(value.toLong())
        "m" -> Duration.ofMinutes(value.toLong())
        else -> throw UnsupportedOperationException("Only s (seconds) and m (minutes) are currently supported.")
    }

}