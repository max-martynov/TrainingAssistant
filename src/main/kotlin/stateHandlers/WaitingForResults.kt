package stateHandlers

import Client
import ClientsRepository
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import keyboards.PaymentKeyboard
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.LocalTime

class WaitingForResultsHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val qiwiApiClient: QiwiApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (client.interviewResults.size == client.interview.interviewQuestions.size) { // he's already received 4 plans -> should wait until end of month
            vkApiClient.sendMessageSafely(
                client.id,
                "Вы сможете продлить подписку и продолжить тренировки ${calculateEndDateOfSubscription(client.daysPassed)}."
            )
            return@coroutineScope
        }
        val answerNumber = client.interview.findAnswerNumberOnKthQuestion(text, client.interviewResults.size)
        if (answerNumber == -1) {
            vkApiClient.sendMessageSafely(
                client.id,
                "Выберите, пожалуйста, один из предложенных вариантов ответа."
            )
        } else {
            if (client.interview.interviewQuestions.size == 4 && client.interviewResults.size == 2 && answerNumber == 1) {
                clientsRepository.update(
                    client.id,
                    newInterviewResults = (client.interviewResults + 1 + 1).toMutableList()
                )
            } else if (client.interview.interviewQuestions.size == 3 && client.interviewResults.size == 1 && answerNumber == 1) {
                clientsRepository.update(
                    client.id,
                    newInterviewResults = (client.interviewResults + 1 + 1).toMutableList()
                )
            } else {
                clientsRepository.update(
                    client.id,
                    newInterviewResults = (client.interviewResults + answerNumber).toMutableList()
                )
            }
            val updatedClient = clientsRepository.findById(client.id) ?: throw Exception()
            if (updatedClient.interviewResults.size == client.interview.interviewQuestions.size) {
                val nextTrainingPlan = trainingPlansRepository.determineNextTrainingPlan(updatedClient)
                if (nextTrainingPlan == null) {
                    vkApiClient.sendMessageSafely(
                        client.id,
                        "Опрос заверешен! К сожалению, в данный момент Вы не можете начать цикл, так как за время одной подписки можно получить только 4 плана. " +
                                "Вы сможете продлить подписку и продолжить тренировки ${calculateEndDateOfSubscription(client.daysPassed)}."
                    )
                } else {
                    if (client.trial) {
                        clientsRepository.update(
                            client.id,
                            newStatus = Status.WAITING_FOR_PAYMENT,
                            newTrainingPlan = nextTrainingPlan,
                            newInterviewResults = mutableListOf()
                        )
                        qiwiApiClient.updateBill(client, clientsRepository)
                        requestPaymentToStart(client)
                    } else {
                        clientsRepository.update(
                            client.id,
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
                        vkApiClient.sendMessageSafely(
                            client.id,
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

    private suspend fun requestPaymentToStart(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            "Опрос завершен!\nОсталось только оплатить месячную подписку, и Вы можете приступать к тренировкам!\n" +
                    "Чтобы открыть окно с оплатой, нажмите \"Оплатить подписку\". После совершения платежа нажмите \"Подтвердить оплату\".",
            keyboard = PaymentKeyboard(qiwiApiClient.getPayUrl(client.billId)).keyboard
        )
    }

    private suspend fun sendInterviewQuestion(client: Client, questionNumber: Int) {
        vkApiClient.sendMessageSafely(
            client.id,
            client.interview.interviewQuestions[questionNumber].question,
            keyboard = client.interview.interviewQuestions[questionNumber].toString()
        )
    }

    private fun calculateEndDateOfSubscription(daysPassed: Int): String {
        val leftDays = mapOf(
            setOf(1, 21) to "день",
            setOf(2, 3, 4, 22, 23, 24) to "дня",
            setOf(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 17, 19, 20, 25, 26, 27, 28) to "дней"
        )
        val daysLeft = 28 - daysPassed + if (LocalTime.now().isAfter(LocalTime.of(12, 0))) 1 else 0
        if (daysLeft == 0)
            return "уже сегодня"
        return "через $daysLeft ${leftDays[leftDays.keys.find { it.contains(daysLeft) }]}"
    }
}