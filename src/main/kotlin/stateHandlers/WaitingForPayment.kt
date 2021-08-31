package stateHandlers

import Client
import ClientsRepository
import api.vk.VKApiClient
import kotlinx.coroutines.coroutineScope

class WaitingForPaymentHandler(
    clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        vkApiClient.sendMessageSafely(
            client.id,
            "Если Вы хотите продолжить тренировки, оплатите, пожалуйста, подписку. " +
                    "Для этого нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\""
        )
    }
}
