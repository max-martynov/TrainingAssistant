package stateHandlers

import Client
import ClientsRepository
import VKApiClient
import confirmPayment
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WaitingForPaymentHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "228 337") {
            confirmPayment(client, null)
        } else {
            vkApiClient.sendMessage(
                client.id,
                "Если Вы хотите продолжить тренировки, оплатите, пожалуйста, подписку. " +
                        "Для этого нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\""
            )
        }
    }
}
