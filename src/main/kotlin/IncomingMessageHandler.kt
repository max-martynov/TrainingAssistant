import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
        val newClient = Client(
            id = clientId,
            status = Status.WAITING_FOR_RESULTS,
            totalDaysPassed = 0,
            trainingPlan = trainingPlansRepository.findTrainingPlan(0)!!, //fix it later
            daysInWeekPassed = 0,
            interviewResults = mutableListOf()
        )
        clientRepository.add(newClient)
        sendGreetingsMessage(newClient)
    }
    else if (client.status == Status.WAITING_FOR_RESULTS &&
        (text == "3 часа" || text == "6 часов" || text == "10 часов")) {
        val trainingPlan = when(text) {
            "3 часа" -> trainingPlansRepository.findTrainingPlan(0)
            "6 часов" -> trainingPlansRepository.findTrainingPlan(0)
            else -> trainingPlansRepository.findTrainingPlan(0)
        }!!
        clientRepository.updateClient(
            id = client.id,
            newStatus = Status.NEW_CLIENT,
            newTrainingPlan = trainingPlan
        )
        sendRules(client)
    }
    else if (client.status == Status.WAITING_FOR_RESULTS && text.toInt() in 1..3) { // reply in interview
        val updatedResults = (client.interviewResults + text.toInt()).toMutableList()
        clientRepository.updateClient(
            client.id,
            newInterviewResults = updatedResults
        )
        if (updatedResults.size == interview.size) { // interview is completed
            val nextTrainingPlan = determineNextTrainingPlan(client)
            sendMessageAboutNextWeek(client, nextTrainingPlan)
            clientRepository.updateClient(
                client.id,
                newTrainingPlan = nextTrainingPlan,
            )
        }
        else {
            sendMessage(
                peerId = client.id,
                text = interview[updatedResults.size].question,
                keyboard = interview[updatedResults.size].answers
            )
        }
    }
}

/**
 * TODO - add correct implementation
 */
fun determineNextTrainingPlan(client: Client): TrainingPlan {
    return trainingPlansRepository.findTrainingPlan((client.trainingPlan.id + 1) % 4)!!
}

suspend fun sendMessageAboutNextWeek(client: Client, nextTrainingPlan: TrainingPlan) {
    sendMessage(
        peerId = client.id,
        text = "Искусственный интеллект подобрал для вас такой план на следующую неделю:\n${nextTrainingPlan.link}\n" +
                "Приступайте к тренировкам с завтрашнего дня."
    )
}

suspend fun sendRules(client: Client) {
    val message = "Отличный выбор!\n" +
            "Со следующего дня вы начнете заниматься по плану, подобранному специально для вас:\n" +
            "${client.trainingPlan.link}\n" +
            "Также каждый день я / мы  / бот (от какого лица мы общаемся я пока не решил) буду напоминать вам, какую тренировку следует сделать, " +
            "а в конце недели вам будет предложено пройти опрос, чтобы сформировать план на следующую неделю."

    sendMessage(client.id, message)
}

fun getTrainingPlanFromJson(path: String): TrainingPlan {
    val jsonString = File(path).readText()
    return Json.decodeFromString(jsonString)
}

suspend fun sendGreetingsMessage(client: Client) {
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
                    },
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"6 часов"
                        },
                        "color":"primary"
                    },
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
    sendMessage(client.id, greeting, selectPlanKeyboard)
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

