package stateHandlers

import ClientsRepository
import api.vk.VKApiClient
import client.Client
import client.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WaitingForBeginHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
) : StateHandler() {
    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Начать цикл") {
            async { clientsRepository.update(
                client.id,
                newStatus = Status.ACTIVE
            ) }
            async { sendPlan(client) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Для того, чтобы получить план и начать недельный цикл, нажмите кнопку \"Начать цикл\". Если у Вас возникли вопросы, нажмите \"Обратная связь\"."
            )
        }
    }

    private suspend fun sendPlan(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            client.trainingPlan.plan
        )
    }

}