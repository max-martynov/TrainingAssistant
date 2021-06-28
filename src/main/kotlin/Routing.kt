import com.beust.klaxon.Klaxon
import com.petersamokhin.vksdk.core.api.botslongpoll.VkBotsLongPollApi
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.http.VkOkHttpClient
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.StringReader
import java.nio.ByteBuffer


/*
sealed class Object
data class MessageObject()


data class Message(
    val id: Long,
    val date: Long,
    val peer_id: Long,
    val from_id: Long,
    val text: String,
    val random_id: Long,
    val ref: String,
    val ref_source: String,

)*/


data class MessageEvent(val type: String, val message: MessageNew, val groupId: Long)

data class Event(val type: String, val object_: JsonElement, val groud_id: Long)

fun getType(call: String): String =
    call.substringAfter('"').substringBefore('"')

fun Application.routing() {

    val vkClientSettings = VkSettings(
        httpClient = VkOkHttpClient(),
        apiVersion = 5.122,
        defaultParams = paramsOf("lang" to "en"),
        maxExecuteRequestsPerSecond = 3, // Default is 3. Provide [VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED] to disable the `execute` queue loop
        backgroundDispatcher = Dispatchers.Default,
        json = Json
    )
    val client = VkApiClient(
        id = 205462754,
        token = "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e",
        type = VkApiClient.Type.Community,
        settings = vkClientSettings
    )

    routing {
        post("/") {
            val call = call.receiveText()
            val type = getType(call)
            if (type == "message_new") {
                val messageEvent = Klaxon().parse<MessageEvent>(call)
                val messageReceived = messageEvent?.message
                client.sendMessage {
                    peerId = messageReceived?.message?.fromId
                    message = "Hello, World!"
                }.execute()
            }
        }
    }
}