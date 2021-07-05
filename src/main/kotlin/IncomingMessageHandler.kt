import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


@Serializable
data class MessageEvent(
    val type: String,
    @SerialName("object")
    val message: IncomingMessage,
    @SerialName("group_id")
    val groupId: Long
)


suspend fun handleIncomingMessage(
    notification: String
) {
    val messageEvent = Json { ignoreUnknownKeys = true }.decodeFromString<MessageEvent>(notification)
    val clientId = messageEvent.message.fromId
    val text = messageEvent.message.text


    val client = clientRepository.findClientById(clientId)
    if (client == null) {
        sendGreetingsMessage(clientId)
    }
    else if (text == "3 часа" || text == "6 часов" || text == "10 часов") {
        val trainingPlan = when(text) {
            "3 часа" -> getTrainingPlanFromJson("src/main/resources/TrainingPlans/3hours.json")
            "6 часов" -> getTrainingPlanFromJson("src/main/resources/TrainingPlans/6hours.json")
            else -> getTrainingPlanFromJson("src/main/resources/TrainingPlans/2.json")
        }
        clientRepository.addClient(
            Client(
                id = clientId,
                status = Status.NEW_CLIENT,
                totalDaysPassed = 0,
                trainingPlan = trainingPlan,
                daysInWeekPassed = 0,
                interviewResults = mutableListOf()
            )
        )
    }
}

fun getTrainingPlanFromJson(path: String): TrainingPlan {
    val jsonString = File(path).readText()
    return Json.decodeFromString(jsonString)
}

suspend fun sendGreetingsMessage(peerId: Int) {
    val greeting = "Привет!\n" +
            "Мы замутили супер бота, которой поможет стать вам победителем по жизни.\n" +
            "Сколько часов в неделю вы хотите заниматься?"
    val selectPlanKeyboard = """
        {
            "one_time": false, 
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
            ],
            "inline":true
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

