package eventHandlers

import Client
import ClientsRepository
import Status
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.*
import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import pressStartKeyboard
import productId
import stateHandlers.*
import java.lang.management.ManagementFactory


class IncomingMessageHandler(
    private val clientsRepository: ClientsRepository,
    private val vKApiClient: VKApiClient,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val qiwiApiClient: QiwiApiClient
) {

    suspend fun receiveMessage(incomingMessage: IncomingMessage) {
        val clientId = incomingMessage.fromId
        val text = incomingMessage.text
        val attachments = incomingMessage.attachments

        println("Current number of threads = ${ManagementFactory.getThreadMXBean().threadCount}")

        val client = clientsRepository.findById(clientId)

        if (client == null && attachments.isNotEmpty() && isOurProduct(attachments[0].toString())) {
            registerNewClient(clientId)
        }
        else if (client != null) {
            getAppropriateHandler(client).handle(client, text)
        }
    }

    private fun isOurProduct(attachment: String): Boolean {
        return Json { ignoreUnknownKeys = true }.decodeFromString<Attachment>(attachment).type == "market" &&
                Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<MarketAttachment>(attachment).market.category.id == productId
    }

    private suspend fun registerNewClient(clientId: Int): Unit = coroutineScope {
        async { clientsRepository.add(Client(clientId)) }
        async { sendGreetings(clientId) }
    }

    private suspend fun sendGreetings(peerId: Int) {
        vKApiClient.sendMessageSafely(
            peerId,
            "Здравствуйте!\nСпасибо, что решили попробовать инновационные тренировки по подписке 🤖\n" +
                    "🔹 Если у Вас внизу не отображаются кнопки \"Старт\" и \"Инструкция\", нажмите на значок чуть правее поля для ввода сообещния.\n" +
                    "🔹 Если у Вас есть вопросы о том, как тут все работает, жмите на \"Инструкция\".\n" +
                    "🔹 Если же Вы все поняли и готовы начинать, жмите \"Старт!\".",
            keyboard = pressStartKeyboard
        )
    }

    private fun getAppropriateHandler(client: Client): StateHandler {
        return when(client.status) {
            Status.NEW_CLIENT -> NewClientHandler(clientsRepository, vKApiClient)
            Status.WAITING_FOR_PLAN -> WaitingForPlanHandler(clientsRepository, vKApiClient)
            Status.WAITING_FOR_START -> WaitingForStartHandler(clientsRepository, vKApiClient, trainingPlansRepository)
            Status.ACTIVE -> ActiveClientHandler(clientsRepository, vKApiClient)
            Status.WAITING_FOR_RESULTS -> WaitingForResultsHandler(clientsRepository, vKApiClient, trainingPlansRepository, qiwiApiClient)
            else -> WaitingForPaymentHandler(clientsRepository, vKApiClient)
        }
    }
}





/*
suspend fun handleIncomingMessage(notification: String) = coroutineScope {
    val event = Json { ignoreUnknownKeys = true }.decodeFromString<EventWithMessage>(notification)
    val clientId = event.message.fromId
    val text = event.message.text
    val attachments = event.message.attachments

    println("Current number of threads = ${ManagementFactory.getThreadMXBean().threadCount}")

    val client = clientsRepository.findById(clientId)

    if (client == null && attachments.isNotEmpty() && isOurProduct(attachments[0].toString())) {
        async { clientsRepository.add(Client(clientId)) }
        async { sendGreetings(clientId) }
    } else if (client != null) {
        when (client.status) {
            Status.NEW_CLIENT -> {
                if (text == "Старт!") {
                    sendMainKeyboardWithoutPromocodes(clientId)
                    async { sendSelectTrainingPlan(clientId) }
                    async { clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_PLAN
                    ) }
                } else {
                    sendMessage(
                        clientId,
                        "Если вы готовы начать, жмите \"Старт!\""
                    )
                }
            }
            Status.WAITING_FOR_PLAN -> {
                if (text == "6 часов" || text == "10 часов") {
                    async { clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_START,
                        newTrainingPlan = TrainingPlan(
                            LocalDate.now().monthValue,
                            if (text == "6 часов") 6 else 10,
                            0
                        )
                    ) }
                    async { sendTrialMessage(clientId) }
                } else {
                    sendMessage(
                        clientId,
                        "Выберите, пожалуйста, сколько часов в неделю хотите тренироваться."
                    )
                }
            }
            Status.WAITING_FOR_START -> {
                if (text == "Начать цикл") {
                    async { clientsRepository.update(
                        clientId,
                        newStatus = Status.ACTIVE,
                        newWeeksPassed = client.weeksPassed + 1
                    ) }
                    async { sendPlan(client) }
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
                        if (client.trial)
                            "Поздравляю с окончанием пробной недели!\nДля формирования следующего плана, пройдите, пожалуйста, небольшой опрос."
                        else
                            phrases.random()
                    )
                    async { clientsRepository.update(
                        clientId,
                        newStatus = Status.WAITING_FOR_RESULTS
                    ) }
                    async { sendInterviewQuestion(client, 0) }
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
                    return@coroutineScope
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
                    } else if (client.interview.interviewQuestions.size == 3 && client.interviewResults.size == 1 && answerNumber == 1) {
                        clientsRepository.update(
                            clientId,
                            newInterviewResults = (client.interviewResults + 1 + 1).toMutableList()
                        )
                    } else {
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
                        } else {
                            if (client.trial) {
                                clientsRepository.update(
                                    clientId,
                                    newStatus = Status.WAITING_FOR_PAYMENT,
                                    newTrainingPlan = nextTrainingPlan,
                                    newInterviewResults = mutableListOf()
                                )
                                client.updateBill()
                                requestPaymentToStart(client)
                            } else {
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
                        }
                    } else {
                        sendInterviewQuestion(
                            client,
                            updatedClient.interviewResults.size
                        )
                    }
                }
            }
            Status.WAITING_FOR_PAYMENT -> {
                if (text == "228 337") {
                    confirmPayment(client, null)
                } else {
                    sendMessage(
                        clientId,
                        "Если Вы хотите продолжить тренировки, оплатите, пожалуйста, подписку. " +
                                "Для этого нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\""
                    )
                }
            }
        }
    }
}





suspend fun sendMainKeyboardWithoutPromocodes(peerId: Int) {
    sendMessage(
        peerId,
        "Отлично! Для начала нужно выбрать нагруженность недельного цикла: пока что есть 2 опции - 6 или 10 часов.",
        keyboard = mainKeyboardWithoutPromocodes
    )
}

suspend fun sendSelectTrainingPlan(peerId: Int) {
    sendMessage(
        peerId,
        "Сколько часов в неделю у Вас есть возможность тренироваться?",
        keyboard = selectHoursKeyboard
    )
}

suspend fun sendTrialMessage(peerId: Int) {
    sendMessage(
        peerId,
        "Хорошие новости! Чтобы Вы попробовали обновленные тренировки по подписке, не рискую своими деньгами, первая неделя у нас в подарок 🎁\n" +
                "Нажмите \"Начать цикл\", чтобы получить план и начать недельный цикл."
    )
}

suspend fun sendPlan(client: Client) {
    val ids = client.trainingPlan.prepareAsAttachment(client.id)
    val phrases = listOf(
        "Хороших тренировок!",
        "Удачных тренировок!"
    )
    val phrase = if (client.trainingPlan.hours == 1)
        "Хорошего восстановления!"
    else
        phrases.random()
    sendMessage(
        client.id,
        phrase,
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

suspend fun requestPaymentToStart(client: Client) {
    sendMessage(
        client.id,
        "Опрос завершен!\nОсталось только оплатить месячную подписку, и Вы можете приступать к тренировкам!\n" +
                "Чтобы открыть окно с оплатой, нажмите \"Оплатить подписку\". После совершения платежа нажмите \"Подтвердить оплату\".",
        keyboard = getPaymentKeyboard(QiwiAPI.getPayUrl(client.billId))
    )
}*/

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
 *  - Пробная неделя
 *  - Добавить постоянную кнопку промокоды
 *  - 29 июля
 *
 *
 */
