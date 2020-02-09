package chaperone

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import com.uchuhimo.konf.toValue
import mu.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}


fun loadChecks(checksDirectory: File): List<Check> {
    check(checksDirectory.isDirectory)
    val checksFiles = checksDirectory.listFiles()
    if (checksFiles == null || checksFiles.isEmpty()) {
        throw IllegalStateException("checks directory is empty: ${checksDirectory.name}")
    }

    return checksFiles.map { checksFile ->
        Config()
            .from.toml.file(checksFile)
            .toValue<Check>()
    }


}