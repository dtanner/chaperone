package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus
import chaperone.SlackWriterConfig
import io.kotest.matchers.shouldBe
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.junit.jupiter.api.Test

class SlackWriterTest {
    private val port = 19000

    private val writer = SlackWriter(
        SlackWriterConfig(
            webhook = "http://localhost:$port",
            onlyWriteFailures = true
        )
    )

    private val failResult = CheckResult(
        name = "sample check name",
        status = CheckStatus.FAIL,
        tags = mapOf("env" to "dev"),
        output = "sample fail output"
    )

    class RecordingServer : HttpHandler {
        val requestBodies = mutableListOf<String>()

        override fun invoke(request: Request): Response {
            requestBodies.add(request.bodyString())
            return Response(Status.OK)
        }
    }

    @Test
    fun `validate the request made to slack`() {
        val app = RecordingServer()

        val server = app.asServer(Undertow(port)).start()
        writer.write(failResult)
        server.stop()

        app.requestBodies.size.shouldBe(1)
        val requestBody = app.requestBodies.first()
        // this is ugly - should be converted to json or use the json matcher of kotest
        requestBody.shouldBe("""{"attachments":[{"title":"sample check name","color":"danger","fields":[{"title":"Output","value":"sample fail output","short":false},{"title":"env","value":"dev","short":true}]}]}""".trimIndent())
    }
}
