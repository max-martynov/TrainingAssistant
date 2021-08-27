package stateHandlers

import Client
import ClientsRepository
import ApiClients.VKApiClient
import PaymentChecker
import kotlinx.coroutines.coroutineScope

class WaitingForPaymentHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
    private val paymentChecker: PaymentChecker
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "228 337") {
            paymentChecker.updateClient(client)
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Если Вы хотите продолжить тренировки, оплатите, пожалуйста, подписку. " +
                        "Для этого нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\""
            )
        }
    }


}
