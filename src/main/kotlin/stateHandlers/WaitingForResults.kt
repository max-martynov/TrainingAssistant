package stateHandlers

import Client
import ClientsRepository
import TrainingPlan
import VKApiClient
import determineNextTrainingPlan
import getPaymentKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class WaitingForResultsHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (client.interviewResults.size == client.interview.interviewQuestions.size) { // he's already received 4 plans -> should wait until end of month
            vkApiClient.sendMessage(
                client.id,
                "Пожалуйста, дождитесь окончания месяца и продлите подписку, чтобы продолжить тренироваться."
            )
            return@coroutineScope
        }
        val answerNumber = client.interview.findAnswerNumberOnKthQuestion(text, client.interviewResults.size)
        if (answerNumber == -1) {
            vkApiClient.sendMessage(
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
                val nextTrainingPlan = determineNextTrainingPlan(updatedClient)
                if (nextTrainingPlan == null) {
                    vkApiClient.sendMessage(
                        client.id,
                        "Опрос заверешен! К сожалению, в данный момент Вы не можете начать цикл, так как за месяц можно получить только 4 плана. " +
                                "Пожалуйста, дождитесь окончания месяца и продлите подписку, чтобы продолжить тренироваться."
                    )
                } else {
                    if (client.trial) {
                        clientsRepository.update(
                            client.id,
                            newStatus = Status.WAITING_FOR_PAYMENT,
                            newTrainingPlan = nextTrainingPlan,
                            newInterviewResults = mutableListOf()
                        )
                        client.updateBill()
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
                        vkApiClient.sendMessage(
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
        vkApiClient.sendMessage(
            client.id,
            "Опрос завершен!\nОсталось только оплатить месячную подписку, и Вы можете приступать к тренировкам!\n" +
                    "Чтобы открыть окно с оплатой, нажмите \"Оплатить подписку\". После совершения платежа нажмите \"Подтвердить оплату\".",
            keyboard = getPaymentKeyboard(QiwiAPI.getPayUrl(client.billId))
        )
    }

    private suspend fun sendInterviewQuestion(client: Client, questionNumber: Int) {
        vkApiClient.sendMessage(
            client.id,
            client.interview.interviewQuestions[questionNumber].question,
            keyboard = client.interview.interviewQuestions[questionNumber].toString()
        )
    }
}