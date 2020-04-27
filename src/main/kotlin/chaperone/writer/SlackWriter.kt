package chaperone.writer

import chaperone.CheckResult
import chaperone.CheckStatus.FAIL
import chaperone.CheckStatus.OK
import chaperone.SlackOutputConfig
import chaperone.json.objectMapper
import mu.KotlinLogging
import okhttp3.OkHttpClient
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request

class SlackWriter(private val config: SlackOutputConfig) : OutputWriter {

    private val log = KotlinLogging.logger {}
    private val client = OkHttp(OkHttpClient())

    override fun write(checkResult: CheckResult) {
        if (config.onlyWriteFailures && checkResult.status != FAIL) return

        postMessage(config.webhook, buildSlackMessage(checkResult))
    }

    private fun buildSlackMessage(checkResult: CheckResult): SlackMessage {
        val fields = mutableListOf<Field>()
        fields.add(Field(title = "Output", value = "${checkResult.output}", short = false))
        fields.addAll(checkResult.tags.map { tag ->
            Field(title = tag.key, value = tag.value)
        })

        return SlackMessage(
            attachments = listOf(
                Attachment(
                    title = checkResult.name,
                    color = if (checkResult.status == OK) "good" else "danger",
                    fields = fields
                )
            )
        )
    }

    private fun postMessage(webhook: String, message: SlackMessage) {
        val request = Request(Method.POST, webhook)
            .header("Content-Type", "application/json")
            .body(objectMapper.writeValueAsString(message))

        val response = client(request)
        if (response.status.code != 200) {
            log.error { "Unexpected status posting to slack. code=${response.status.code} message=${response.bodyString()}" }
        }
    }

}

data class SlackMessage(
    val attachments: List<Attachment> = emptyList()
)

// https://api.slack.com/reference/messaging/attachments
data class Attachment(
    val title: String,
    val color: String,
    val fields: List<Field> = emptyList()
)

data class Field(
    val title: String,
    val value: String,
    val short: Boolean = true
)