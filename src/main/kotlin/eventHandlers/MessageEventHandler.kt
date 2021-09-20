package eventHandlers

import Client
import ClientsRepository
import Status
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.*
import keyboards.MainKeyboardWithPromocodes
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MessageEventHandler(
    private val clientsRepository: ClientsRepository,
    private val vKApiClient: VKApiClient,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val qiwiApiClient: QiwiApiClient
) {
    suspend fun checkPayment(messageEvent: MessageEvent) = coroutineScope {
        val client = clientsRepository.findById(messageEvent.userId) ?: return@coroutineScope
        if (client.status != Status.WAITING_FOR_PAYMENT) {
            vKApiClient.sendMessageEventAnswerSafely(messageEvent, getShowSnackbarString("Оплата прошла успешно. Хороших тренировок!"))
        } else if (qiwiApiClient.isBillPaid(client.billId)) {
            sendThanks(client)
            confirmPayment(client, messageEvent)
        } else {
            vKApiClient.sendMessageEventAnswerSafely(
                messageEvent,
                getShowSnackbarString("К сожалению, данные об оплате еще не поступили! Попробуйте позже.")
            )
        }
    }

    private suspend fun confirmPayment(client: Client, messageEvent: MessageEvent?) {
        val phrase =
            if (client.trial)
                "Оплата подтверждена! Спасибо, что решили продолжить тренировки по подписке."
            else
                "Оплата подтверждена! Надеюсь, Вам понравятся тренировки в этом месяце."
        updateClient(client)
        if (messageEvent != null)
            vKApiClient.sendMessageEventAnswerSafely(messageEvent, getShowSnackbarString(phrase))
    }

    private suspend fun updateClient(client: Client) {
        if (client.trial) { // for clients after trial
            clientsRepository.update(
                client.id,
                newTrial = false,
                newStatus = Status.WAITING_FOR_START,
                newWeeksPassed = 0,
                newDaysPassed = 0
            )
        } else if (client.previousStatus == Status.WAITING_FOR_RESULTS && client.completedInterview()) { // for those who completed month too fast
            clientsRepository.update(
                client.id,
                newWeeksPassed = 0,
                newDaysPassed = 0
            )
            val updatedClient = clientsRepository.findById(client.id)!!
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_START,
                newTrainingPlan = trainingPlansRepository.determineNextTrainingPlan(updatedClient),
                newInterviewResults = mutableListOf()
            )
        } else { // for usual clients
            clientsRepository.update(
                client.id,
                newStatus = client.previousStatus,
                newDaysPassed = 0,
                newWeeksPassed = 0
            )
        }
    }

    private fun getShowSnackbarString(text: String): String =
        """
        {
            "type": "show_snackbar", 
            "text": "$text"
        }
    """.trimIndent()

    private suspend fun sendThanks(client: Client) {
        if (client.trial) {
            vKApiClient.sendMessageSafely(
                client.id,
                "Впереди месяц интересных тренировок! Подписка будет действовать 28 дней и за это время Вы сможете получить ровно 4 плана.\n" +
                        "Для того, чтобы начать тренировочный процесс, нажмите \"Начать цикл\" (если Вы не видите этой кнопки, нажмите на кнопку чуть правее поля для ввода сообщения).\n" +
                        "Также не забывайте, что Вам теперь доступны промокоды 🎁",
                keyboard = MainKeyboardWithPromocodes().keyboard
            )
        }
        else {
            vKApiClient.sendMessageSafely(
                client.id,
                "Спасибо, что продолжаете тренироваться вместе с нами! Подписка будет действовать 28 дней и за это время Вы сможете получить ровно 4 плана.\n" +
                        "Для того, чтобы начать тренировочный процесс, нажмите \"Начать цикл\".\n"
            )
        }
    }
}