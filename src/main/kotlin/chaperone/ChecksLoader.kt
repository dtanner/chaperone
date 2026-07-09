package chaperone

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import java.io.File

fun loadChecks(checksDirectory: File): List<Check> {
    check(checksDirectory.isDirectory) { "Error: checksDirectory wasn't found. [${checksDirectory.path}]" }

    val checks: MutableList<Check> = mutableListOf()
    checksDirectory.walkTopDown().forEach {
        if (it.isFile && it.name.endsWith(suffix = "toml", ignoreCase = true)) {
            // todo if it becomes an issue - validate this toml file is indeed a check and not just a toml file used by the check
            val check: Check = ConfigLoaderBuilder.default()
                .addFileSource(it)
                .build()
                .loadConfigOrThrow<Check>()
            checks.add(check.copy(fileDirectory = it.parentFile))
        }
    }
    check(checks.isNotEmpty()) { "no checks were found in ${checksDirectory.path}" }
    return checks
}
