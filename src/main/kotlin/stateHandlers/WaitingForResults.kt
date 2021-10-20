package stateHandlers

import Client
import ClientsRepository
import ReviewKeyboard
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
        val answerNumber = client.interview.findAnswerNumberOnKthQuestion(text, client.interviewResults.size)
        if (answerNumber == -1) {
            vkApiClient.sendMessageSafely(
                client.id,
                "Выберите, пожалуйста, один из предложенных вариантов ответа."
            )
        } else {
            if (client.interview.interviewQuestions.size == 5 && client.interviewResults.size == 2 && answerNumber == 1) {
                clientsRepository.update(
                    client.id,
                    newInterviewResults = (client.interviewResults + 1 + 1).toMutableList()
                )
            } else if (client.interview.interviewQuestions.size == 4 && client.interviewResults.size == 1 && answerNumber == 1) {
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
                logReview(updatedClient)
                val nextTrainingPlan = trainingPlansRepository.determineNextTrainingPlan(updatedClient)
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

    private fun logReview(client: Client) {
        println("----\nNew review from client with id ${client.id} on training plan ${client.trainingPlan}: ${client.interviewResults.last() + 1}\n----")
    }
}