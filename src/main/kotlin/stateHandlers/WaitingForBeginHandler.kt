package stateHandlers

import ClientsRepository
import api.vk.VKApiClient
import client.Client
import client.Status
import keyboards.FinishKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WaitingForBeginHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
) : StateHandler() {
    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Начать план") {
            async { clientsRepository.update(
                client.id,
                newStatus = Status.ACTIVE
            ) }
            async { sendPlan(client) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Для того, чтобы получить план и начать тренироваться, нажмите кнопку \"Начать план\"."
            )
        }
    }

    private suspend fun sendPlan(client: Client) {
        vkApiClient.sendMessageSafely(
            client.id,
            client.trainingPlan.plan,
            keyboard = FinishKeyboard().getKeyboard()
        )
    }

}