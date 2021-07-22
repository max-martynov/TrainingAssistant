import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import com.petersamokhin.vksdk.core.model.event.MessageNew
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
import java.time.LocalDate
import java.util.*
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong


suspend fun receivePayment(notification: String) {
    @Serializable
    data class Payment(
        @SerialName("from_id")
        val fromId: Int,
        val amount: Int
    )
    @Serializable
    data class PaymentEvent(
        val type: String,
        @SerialName("object")
        val payment: Payment,
        @SerialName("group_id")
        val groupId: Long
    )

    val paymentInfo = Json { ignoreUnknownKeys = true }.decodeFromString<PaymentEvent>(notification).payment
    val fromId = paymentInfo.fromId
    val amount = paymentInfo.amount
    val client = clientsRepository.findById(fromId) ?: return
    if (amount == 1000 && client.status == Status.WAITING_FOR_PAYMENT) {
        val phrases = listOf(
            "Подписка успешно продлена! Впереди месяц разнообразных тренировок.",
            "Подписка успешно продлена! Надеюсь, Вам понравятся тренировки в этом месяце."
        )
        if (client.isNew()) { // brand new client
            sendMessage(
                client.id,
                "Подписка успешно оформлена! Для того, чтобы получить план и начать недельный цикл, нажмите \"Начать цикл\"."
            )
            clientsRepository.update(
                fromId,
                newStatus = Status.WAITING_FOR_START,
                newDaysPassed = 0
            )
        }
        else if (client.previousStatus == Status.WAITING_FOR_RESULTS && client.completedInterview()) { //
            clientsRepository.update(
                fromId,
                newWeeksPassed = 0,
                newDaysPassed = 0
            )
            val updatedClient = clientsRepository.findById(fromId)!!
            clientsRepository.update(
                fromId,
                newStatus = Status.WAITING_FOR_START,
                newTrainingPlan = determineNextTrainingPlan(updatedClient),
                newInterviewResults = mutableListOf()
            )
            sendMessage(
                fromId,
                phrases.random()
            )
        }
        else { // not new, just notify that subscription is fine
            sendMessage(
                client.id,
                phrases.random()
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


suspend fun handleIncomingMessage(
    notification: String
) {
    val messageEvent = Json { ignoreUnknownKeys = true }.decodeFromString<MessageEvent>(notification)
    val clientId = messageEvent.message.fromId
    val text = messageEvent.message.text
    val attachments = messageEvent.message.attachments

    val client = clientsRepository.findById(clientId)

    if (client == null && attachments.isNotEmpty() && isOurProduct(attachments[0].toString())) {
        clientsRepository.add(
            Client(clientId)
        )
        sendGreetings(clientId)
        clientsRepository.update(
            clientId,
            newStatus = Status.NEW_CLIENT
        )
    } else if (client != null) {
        when (client.status) {
            Status.NEW_CLIENT -> {
                if (text == "Старт!") {
                    sendMainKeyboard(clientId)
                    sendSelectTrainingPlan(clientId)
                    clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_PLAN
                    )
                } else {
                    sendMessage(
                        clientId,
                        "Если вы готовы начать, жмите на \"Старт!\""
                    )
                }
            }
            Status.WAITING_FOR_PLAN -> {
                if (text == "6 часов" || text == "10 часов") {
                    clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_PAYMENT,
                        newWeeksPassed = 0,
                        newTrainingPlan = TrainingPlan(LocalDate.now().monthValue, if (text == "6 часов") 6 else 10, 0)
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
                        "{\"type\":\"vkpay_transaction\",\"object\":{\"amount\":1000,\"from_id\":217619042,\"description\":\"\",\"date\":1626875771},\"group_id\":205462754,\"event_id\":\"cbfb3d0db7480848dd90cdb2134d4d99387f61e6\",\"secret\":\"EWmBzU9QTeXtVTYe7nQ8Nh6y3WPgaPM\"}"
                    )
                } else if (text != "") {
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
                        newStatus = Status.ACTIVE,
                        newWeeksPassed = client.weeksPassed + 1
                    )
                    sendPlan(client)
                } else {
                    sendMessage(
                        clientId,
                        "Для того, чтобы получить план и начать недельный цикл, нажмите \"Начать цикл\"."
                    )
                }
            }
            Status.ACTIVE -> {
                if (text == "Закончить цикл") {
                    val phrases = listOf(
                        "Поздравляю с окончанием недельного цикла!\nЧтобы сформировать план на следующую неделю, пройдите, пожалуйста, небольшой опрос.",
                        "Отличная работа!\nДля формирования плана на следующую неделю, пройдите, пожалуйста, небольшой опрос.",
                        "Недельный цикл успешно завершен! Пройдите, пожалуйста, небольшой опрос, чтобы сформировать план на следующую неделю."
                    )
                    sendMessage(
                        clientId,
                        phrases.random()
                    )
                    clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_RESULTS
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
                            val phrases = listOf(
                                "Опрос завершен! На основании его результатов для Вас был подобран уникальный тренировочный план.\n" +
                                        "Чтобы увидеть его и начать тренировочный процесс, нажмите \"Начать цикл\".",
                                "Опрос подошел к концу. Спасибо за Ваши ответы! На основании них для Вас был подобран уникальный тренировочный план.\n" +
                                        "Чтобы увидеть его и начать тренировочный процесс, нажмите \"Начать цикл\"."
                            )
                            sendMessage(
                                clientId,
                                phrases.random()
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

fun isOurProduct(attachment: String): Boolean {
    @Serializable
    data class Attachment(val type: String)
    @Serializable
    data class Category(val id: Int)
    @Serializable
    data class Market(val category: Category)
    @Serializable
    data class MarketAttachment(val market: Market)

    return Json { ignoreUnknownKeys = true }.decodeFromString<Attachment>(attachment).type == "market" &&
            Json {
                ignoreUnknownKeys = true
            }.decodeFromString<MarketAttachment>(attachment).market.category.id == productId
}

suspend fun sendPlan(client: Client) {
    val ids = client.trainingPlan.prepareAsAttachment(client.id)
    val phrases = listOf(
        "Хороших тренировок!",
        "Удачных тренировок!"
    )
    sendMessage(
        client.id,
        phrases.random(),
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

suspend fun requestPaymentToStart(peerId: Int, toGroup: Int = groupId, amount: Int = 500) {
    val phrases = listOf(
        "Отличный выбор!\nЧтобы увидеть план и начать тренироваться, оплатите месячную подписку.",
        "Хороший выбор!\nОсталось только оплатить месячную подписку и Вы можете приступать к тренировкам.",
        "Превосходно!\nВсе, что Вам осталось, это оплатить месячную подписку."
    )
    sendMessage(
        peerId,
        phrases.random(),
        keyboard = """
            {
                "one_time": false,
                "buttons": [
                    [
                        {
                            "action":{ 
                                "type":"vkpay", 
                                "hash":"action=pay-to-group&group_id=$toGroup&amount=$amount&aid=7889001" 
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
        "Здравствуйте!\nСпасибо, что решили попробовать обновленные тренировки по подписке 🤖\n" +
                "Если у Вас есть вопросы о том, как тут все работает, жмите на \"Краткое руководство\". " +
                "Специально для Вас мы написали подробную статью, чтобы процесс взаимодействия с чат-ботом был простым и удобным 👍\n" +
                "Если же вы все поняли и готовы начинать, жмите на \"Старт!\".",
        keyboard = """
            {
                "one_time":false,
                "buttons":[
                     [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@-205462754-chat-bot-kratkoe-rukovodstvo",
                                "label":"Краткое руководство"
                            }
                        }, {
                            "action":{
                                "type":"text",
                                "label":"Старт!"
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

suspend fun sendMainKeyboard(peerId: Int) {
    sendMessage(
        peerId,
        "Отлично! Для начала нужно выбрать нагруженность недельного цикла: пока что есть 2 опции - 6 или 10 часов.",
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
                     ], [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@-205462754-chat-bot-kratkoe-rukovodstvo",
                                "label":"Краткое руководство"
                            }
                        }
                     ], [   
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
        parameter("access_token", accessToken)
        parameter("peer_id", peerId)
        parameter("message", text)
        parameter("keyboard", keyboard)
        parameter("attachment", attachment)
        parameter("v", "5.81")
    }
    println(response.content.readUTF8Line())
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
 * "action": {
"type": "vkpay",
"payload": "{\"button\": \"1\"}",
"hash": "action=pay-to-user&amount=5&description=aaaa&user_id=15733972&aid=7889001"
}
 *
 */
