import com.beust.klaxon.Klaxon
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.http.VkOkHttpClient
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

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

data class Message(
    val user_id: Int,
    val message: String = "Hello, nigga"
)

data class Event(val type: String, val object_: JsonElement, val groud_id: Long)

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
            val event = call.receive<Event>()
            if (event.type == "message_new") {
                val messages = Klaxon().parse<MessageNew>(event.object_.toString())
                if (messages != null) {
                    client.sendMessage {
                        peerId = messages.message.fromId
                        message = "Hello, World!"
                    }.execute()
                }
            }
        }
    }
}