import com.beust.klaxon.Klaxon
import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.http.VkOkHttpClient
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.SerialName
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

data class MyMessage(
    val id: Int,
    val date: Int,
    @SerialName("from_id")
    val fromId: Int,
    @SerialName("random_id")
    val randomId: Int? = null,
    val text: String,
    val attachments: List<JsonElement>,
    @SerialName("conversation_message_id")
    val conversationMessageId: Int,
    @SerialName("peer_id")
    val peerId: Int,
    val out: Int? = null,
    @SerialName("update_time")
    val updateTime: Int? = null,
    val isHidden: Boolean? = null,
    val important: Boolean,
    val fwdMessages: List<MessagePartial>
)

data class TypingState(val state: String, val fromId: Int, val toId: Int)

data class TypingStateEvent(val type: String, val state: TypingState, val groupId: Long)

//data class MessageEvent(val type: String, val messagege: MyMessage, val groupId: Long)

//data class Event(val type: String, val object_: JsonElement, val groud_id: Long)

fun getType(call: String): String =
    call.substring(9, call.indexOf('"', 9))

fun Application.routing() {

    val vkClientSettings = VkSettings(
        httpClient = VkOkHttpClient(),
        apiVersion = 5.80,
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
            println(call)
            val type = getType(call)
            if (type == "message_typing_state") {
                val typingStateEvent = Klaxon().parse<TypingStateEvent>(call)
                client.sendMessage {
                    peerId = typingStateEvent?.state?.fromId
                    message = typingStateEvent?.state?.state + "..." + typingStateEvent?.state?.state
                }.execute()
                /*val messageReceived = messageEvent?.messagege
                client.sendMessage {
                    peerId = messageReceived?.fromId
                    message = messageReceived?.text
                }.execute()*/
            }
        }
    }
}