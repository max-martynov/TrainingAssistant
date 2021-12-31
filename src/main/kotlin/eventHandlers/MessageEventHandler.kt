package eventHandlers

import client.Client
import ClientsRepository
import client.Status
import api.qiwi.QiwiApiClient
import api.vk.*
import keyboards.MainKeyboardAfterPayment
import kotlinx.coroutines.coroutineScope

class MessageEventHandler(
    private val clientsRepository: ClientsRepository,
    private val vKApiClient: VKApiClient,
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
        val phrase = "Оплата подтверждена. Хороших тренировок в этом месяце!"
        updateClient(client)
        if (messageEvent != null)
            vKApiClient.sendMessageEventAnswerSafely(messageEvent, getShowSnackbarString(phrase))
    }

    private suspend fun updateClient(client: Client) {
        if (client.trialPeriodEnded) {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_START,
            )
        }
        else {
            clientsRepository.update(
                client.id,
                newStatus = client.previousStatus,
                newDaysPassed = 0
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
        if (client.trialPeriodEnded) {
            vKApiClient.sendMessageSafely(
                client.id,
                "Впереди месяц интересных тренировок! Подписка будет действовать 28 дней и по истечению этого срока Вам автоматически будет предложено продлить её.\n" +
                        "Для того, чтобы начать тренировочный процесс, нажмите кнопку \"Начать цикл\".\n" +
                        "Также не забывайте, что теперь Вам доступен раздел \"Полезное\", в котором Вы всегда сможете найти:\n" +
                        " \uD83D\uDD39 промокоды от партнеров\n" +
                        " \uD83D\uDD39 мотивационную подборку.",
                keyboard = MainKeyboardAfterPayment().getKeyboard()
            )
        }
        else {
            vKApiClient.sendMessageSafely(
                client.id,
                "Спасибо, что продолжаете тренироваться вместе со мной! Подписка будет действовать 28 дней и по истечению этого срока Вам автоматически будет предложено продлить её."
            )
        }
    }
}