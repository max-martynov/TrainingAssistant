package stateHandlers

import client.Client
import ClientsRepository
import api.vk.VKApiClient
import client.Status
import keyboards.MainKeyboardAfterPayment
import keyboards.StartKeyboard
import kotlinx.coroutines.coroutineScope

class WaitingForPaymentHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler() {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "228") {
            sendThanks(client)
            updateClient(client)
        }
        else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Если Вы хотите продолжить тренировки, оплатите, подписку. " +
                        "Для этого нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\"."
            )
        }
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


    private suspend fun sendThanks(client: Client) {
        if (client.trialPeriodEnded) {
            vkApiClient.sendMessageSafely(
                client.id,
                "Впереди месяц интересных тренировок! Подписка будет действовать 28 дней и по истечению этого срока Вам автоматически будет предложено продлить её.\n" +
                        "Не забывайте, что теперь Вам доступен раздел \"Полезное\", в котором Вы всегда сможете найти:\n" +
                        " \uD83D\uDD39 промокоды от партнеров\n" +
                        " \uD83D\uDD39 мотивационную подборку.",
                keyboard = MainKeyboardAfterPayment().getKeyboard()
            )
            vkApiClient.sendMessageSafely(
                client.id,
                "Для того, чтобы продолжить тренировочный процесс, нажмите кнопку ниже.\n",
                keyboard = StartKeyboard().getKeyboard()
            )
        }
        else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Спасибо, что продолжаете тренироваться вместе со мной! Подписка будет действовать 28 дней и по истечению этого срока Вам автоматически будет предложено продлить её."
            )
        }
    }
}
