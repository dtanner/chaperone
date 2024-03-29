package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.InfluxDbWriterConfig
import io.kotest.matchers.shouldBe
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import java.time.Instant

class InfluxDbWriterTest {

    private val httpServerPort = 9000
    private val writer = InfluxDbWriter(config = InfluxDbWriterConfig(db = "a", uri = "http://localhost:$httpServerPort"))

    @Test
    fun `generate line`() {
        val checkResult = CheckResult(name = "sample check", status = CheckStatus.OK)
        val tags = mapOf("x" to "x", "tagy" to "tag y")
        val timestamp = Instant.now().toEpochMilli()

        val line = writer.generateLine(checkResult, tags, timestamp)

        line.shouldBe("""check_status_code,x=x,tagy=tag\ y value=0i $timestamp""")
    }

    @Test
    fun `write test`() {
        val app = { _: Request -> Response(Status.NO_CONTENT) }
        val server = app.asServer(Undertow(httpServerPort)).start()

        val checkResult = CheckResult(name = "sample check", status = CheckStatus.OK)
        writer.write(checkResult)

        server.stop()
    }

}
