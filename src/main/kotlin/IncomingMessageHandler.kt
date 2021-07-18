import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.utils.io.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File


suspend fun receivePayment(
    notification: String
) {
    @Serializable
    data class PaymentEvent(
        @SerialName("from_id")
        val fromId: Int
    )

    val fromId = Json { ignoreUnknownKeys = true }.decodeFromString<PaymentEvent>(notification).fromId
    val client = clientsRepository.findById(fromId) ?: return
    if (client.status == Status.WAITING_FOR_PAYMENT) {
        if (client.daysPassed == -1) {
            sendMessage(
                client.id,
                "Подписка успешно оформлена! Для того, чтобы получить план и начать недельный цикл, нажмите на \"Начать цикл\"."
            )
            clientsRepository.update(
                fromId,
                newStatus = Status.WAITING_FOR_START,
                newDaysPassed = 0
            )
        }
        else if (client.previousStatus == Status.WAITING_FOR_RESULTS && client.completedInterview()) {
            clientsRepository.update(
                fromId,
                newStatus = Status.WAITING_FOR_START,
                newTrainingPlan = determineFirstTrainingPlan(client),
                newWeeksPassed = 0,
                newDaysPassed = 0,
                newInterviewResults = mutableListOf()
            )
            sendMessage(
                client.id,
                "Подписка успешно оформлена! Для того, чтобы получить план и начать недельный цикл, нажмите на \"Начать цикл\"."
            )
        }
        else { // not new, just notify that subscription is fine
            sendMessage(
                client.id,
                "Подписка успешно продлена. Хороших тренировок!"
            )
            clientsRepository.update(
                fromId,
                newStatus = client.previousStatus,
                newDaysPassed = 0,
                newWeeksPassed = 0
            )
        }
    }
}

@Serializable
data class MessageEvent(
    val type: String,
    @SerialName("object")
    val message: IncomingMessage,
    @SerialName("group_id")
    val groupId: Long
)

@Serializable
data class Attachment(val type: String)

@Serializable
data class MarketAttachment(val market: Market)

@Serializable
data class Market(val category: Category)

@Serializable
data class Category(val id: Int)


suspend fun handleIncomingMessage(
    notification: String
) {
    val messageEvent = Json { ignoreUnknownKeys = true }.decodeFromString<MessageEvent>(notification)
    val clientId = messageEvent.message.fromId
    val text = messageEvent.message.text
    val attachments = messageEvent.message.attachments


    val client = clientsRepository.findById(clientId)

    if (client == null &&
        attachments.isNotEmpty() &&
        Json { ignoreUnknownKeys = true }.decodeFromString<Attachment>(attachments[0].toString()).type == "market" &&
        Json {
            ignoreUnknownKeys = true
        }.decodeFromString<MarketAttachment>(attachments[0].toString()).market.category.id == 803
    ) {
        clientsRepository.add(
            Client(clientId)
        )
        sendGreetings(clientId)
        sendSelectTrainingPlan(clientId)
    } else if (client != null) {
        when (client.status) {
            Status.WAITING_FOR_PLAN -> {
                if (text == "6 часов" || text == "10 часов") {
                    clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_PAYMENT,
                        newWeeksPassed = 0,
                        newTrainingPlan = determineFirstTrainingPlan(client, if (text == "6 часов") 6 else 10)
                    )
                    requestPaymentToStart(clientId, amount = 1)
                } else {
                    sendMessage(
                        clientId,
                        "Выберите, пожалуйста, сколько часов в неделю хотите тренироваться."
                    )
                }
            }
            Status.WAITING_FOR_PAYMENT -> {
                if (text == "228") {
                    receivePayment(
                        """
                        {
                            "from_id": ${client.id}
                        }
                    """.trimIndent()
                    )
                } else {
                    sendMessage(
                        clientId,
                        "Оплатите, пожалуйста, подписку."
                    )
                }
            }
            Status.WAITING_FOR_START -> {
                if (text == "Начать цикл") {
                    clientsRepository.update(
                        clientId,
                        newStatus = Status.ACTIVE
                    )
                    sendPlan(client)
                } else {
                    sendMessage(
                        clientId,
                        "Для того, чтобы получить план и начать недельный цикл, нажмите на \"Начать цикл\"."
                    )
                }
            }
            Status.ACTIVE -> {
                if (text == "Закончить цикл") {
                    sendMessage(
                        clientId,
                        "Поздравляю с окончанием недельного цикла!\n" +
                                "Чтобы сформировать план на следующую неделю, пройдите, пожалуйста, небольшой опрос."
                    )
                    clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_RESULTS,
                        newWeeksPassed = client.weeksPassed + 1
                    )
                    sendInterviewQuestion(client, 0)
                } else {
                    sendMessage(
                        clientId,
                        "Для того, чтобы закончить выполнение недельного цикла, нажмите \"Закончить цикл\"."
                    )
                }
            }
            Status.WAITING_FOR_RESULTS -> {
                if (client.interviewResults.size == client.interview.interviewQuestions.size) { // he's already received 4 plans -> should wait until end of month
                    sendMessage(
                        clientId,
                        "Пожалуйста, дождитесь окончания месяца и продлите подписку, чтобы продолжить тренироваться."
                    )
                    return
                }
                val answerNumber = client.interview.findAnswerNumberOnKthQuestion(text, client.interviewResults.size)
                if (answerNumber == -1) {
                    sendMessage(
                        clientId,
                        "Выберите, пожалуйста, один из предложенных вариантов ответа."
                    )
                } else {
                    if (client.interview.interviewQuestions.size == 4 && client.interviewResults.size == 2 && answerNumber == 1) {
                        clientsRepository.update(
                            clientId,
                            newInterviewResults = (client.interviewResults + 1 + 1).toMutableList()
                        )
                    }
                    else if (client.interview.interviewQuestions.size == 3 && client.interviewResults.size == 1 && answerNumber == 1) {
                        clientsRepository.update(
                            clientId,
                            newInterviewResults = (client.interviewResults + 1 + 1).toMutableList()
                        )
                    }
                    else {
                        clientsRepository.update(
                            clientId,
                            newInterviewResults = (client.interviewResults + answerNumber).toMutableList()
                        )
                    }
                    val updatedClient = clientsRepository.findById(clientId) ?: throw Exception()
                    if (updatedClient.interviewResults.size == client.interview.interviewQuestions.size) {
                        val nextTrainingPlan = determineNextTrainingPlan(updatedClient)
                        if (nextTrainingPlan == null) {
                            sendMessage(
                                clientId,
                                "Опрос заверешен! К сожалению, в данный момент Вы не можете начать цикл, так как за месяц можно получить только 4 плана. " +
                                        "Пожалуйста, дождитесь окончания месяца и продлите подписку, чтобы продолжить тренироваться."
                            )
                        }
                        else {
                            clientsRepository.update(
                                clientId,
                                newStatus = Status.WAITING_FOR_START,
                                newTrainingPlan = nextTrainingPlan,
                                newInterviewResults = mutableListOf()
                            )
                            sendMessage(
                                clientId,
                                "Опрос завершен! На основании его результатов для Вас был подобран уникальный тренировочный план. " +
                                        "Чтобы увидеть его и начать тренировочный процесс, нажмите \"Начать цикл\"."
                            )
                        }
                    } else {
                        sendInterviewQuestion(
                            client,
                            updatedClient.interviewResults.size
                        )
                    }
                }
            }
        }
    }
}

suspend fun sendPlan(client: Client) {
    val ids = client.trainingPlan.prepareAsAttachment(client.id)
    //println("(${ids.first}, ${ids.second})")
    sendMessage(
        client.id,
        "Хороших тренировок!",
        attachment = "doc${ids.first}_${ids.second}"
    )
}

suspend fun sendInterviewQuestion(client: Client, questionNumber: Int) {
    sendMessage(
        client.id,
        client.interview.interviewQuestions[questionNumber].question,
        keyboard = client.interview.interviewQuestions[questionNumber].toString()
    )
}

suspend fun requestPaymentToStart(peerId: Int, toUser: Int = 15733972, amount: Int = 500) {
    sendMessage(
        peerId,
        "Отличный выбор!\nЧтобы увидеть план и начать тренироваться, оплатите месячную подписку.",
        keyboard = """
            {
                "one_time": false,
                "buttons": [
                    [
                        {
                            "action": {
                                "type": "vkpay",
                                "hash": "action=pay-to-user&amount=$amount&user_id=$toUser&aid=7889001"
                            }
                        }
                    ]
                ],
                "inline": true
            }
        """.trimIndent()
    )
}

suspend fun sendGreetings(peerId: Int) {
    sendMessage(
        peerId,
        "Здравствуйте!\nСпасибо, что решили воспользоваться нашим чат-ботом.",
        keyboard = """
            {
                "one_time":false,
                "buttons":[
                     [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.me/tuchin_a_95",
                                "label":"Обратная связь"
                            }
                        } 
                     ],
                     [   
                        {
                            "action":{
                                "type":"text",
                                "label":"Начать цикл"
                            },
                            "color":"primary"
                        }, 
                        {
                            "action":{
                                "type":"text",
                                "label":"Закончить цикл"
                            },
                            "color":"primary"
                        }
                    ]
                ],
                "inline": false
            }
        """.trimIndent()
    )
}

suspend fun sendSelectTrainingPlan(peerId: Int) {
    sendMessage(
        peerId,
        "Сколько часов в неделю у Вас есть возможность тренироваться?",
        keyboard = """
        {
            "one_time": false, 
            "buttons":
            [ 
                [ 
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
    )
}

suspend fun sendMessage(peerId: Int, text: String, keyboard: String = "", attachment: String = "") {
    val httpClient: HttpClient = HttpClient()
    val response = httpClient.post<HttpResponse>(
        "https://api.vk.com/method/messages.send?"
    ) {
        parameter(
            "access_token",
            "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e"
        )
        parameter("peer_id", peerId)
        parameter("message", text)
        parameter("keyboard", keyboard)
        parameter("attachment", attachment)
        parameter("v", "5.80")
    }
    //println(response.content.readUTF8Line())
}


/**
 * 1. Скажите, пожалуйста, как Ваше самочувствие после пройденного недельного цикла?
 *  - Устал / утомился
 *  - Чувствую себя нормально
 *  - Чувствую себя легко
 * 2. Нужно ли сделать тренировочный план легче / меньше?
 *  - Да
 *  - Нет
 * 3. Болели ли Вы в течение недельного цикла?
 *  - Да
 *  - Нет
 * 4. Нужно ли вам восстановление? (в случае ответа Нет на предыдущий вопрос, тут автоматически ответ Нет)
 *  - Да
 *  - Нет
 *
 *
 * Постоянная кнопка: Начал выполнение недельног оцикла, Закончил выаплнеие недельного циклва Обратная связь
 * Деньги собирать всегда через 30 дней
 * Может начать план только 4 раза за месяц
 * Забиваем на 3 часа
 *
 */