package chaperone

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import java.io.File

fun loadChecks(checksDirectory: File): List<Check> {
    check(checksDirectory.isDirectory) { "Error: checksDirectory wasn't found. [${checksDirectory.path}]" }
    checksDirectory.listFiles() ?: throw IllegalStateException("checks directory is empty. ${checksDirectory.path}")

    val checks: MutableList<Check> = mutableListOf()
    checksDirectory.walkTopDown().forEach {
        if (it.isFile && it.name.endsWith(suffix = "toml", ignoreCase = true)) {
            // todo if it becomes an issue - validate this toml file is indeed a check and not just a toml file used by the check
            checks.add(Config().from.toml.file(it).toValue())
        }
    }
    return checks
}
