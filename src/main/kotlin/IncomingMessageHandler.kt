import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


@Serializable
data class MessageEvent(
    val type: String,
    @SerialName("object")
    val message: IncomingMessage,
    @SerialName("group_id")
    val groupId: Long
)

suspend fun handleIncomingMessage(notification: String) {
    val messageEvent = Json { ignoreUnknownKeys = true }.decodeFromString<MessageEvent>(notification)
    val clientId = messageEvent.message.fromId
    if (dataBase.containsClient(clientId)) {
        sendMessage(clientId, "План выбран!")
    }
    else {
        val newClient = Client(clientId)
        dataBase.addClient(newClient)
        sendGreetingsMessage(newClient.id)
    }
}

suspend fun sendGreetingsMessage(peerId: Int) {
    val greeting = "Привет!\n" +
            "Тут у нас значится супер бот, который сделает из тебя победителя по жизни.\n" +
            "Выбери подходящий план и кайфуй."
    val selectPlanKeyboard = """
        {
            "one_time":true, 
            "buttons":
            [ 
                [ 
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"3 часа"
                        },
                        "color":"primary"
                    }
                ],
                [ 
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"6 часов"
                        },
                        "color":"primary"
                    }
                ],
                [ 
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"10 часов"
                        },
                        "color":"primary"
                    }
                ]
            ] 
        }
    """.trimIndent()
    sendMessage(peerId, greeting, selectPlanKeyboard)
}

suspend fun sendMessage(peerId: Int, text: String, keyboard: String = "") {
    val httpClient: HttpClient = HttpClient()
    val response = httpClient.post<HttpResponse>(
        "https://api.vk.com/method/messages.send?"
    ) {
        parameter("access_token", "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e")
        parameter("peer_id", peerId)
        parameter("message", text)
        parameter("keyboard", keyboard)
        parameter("v", "5.80")
    }
}

