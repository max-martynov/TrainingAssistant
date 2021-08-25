package stateHandlers

import Client
import ClientsRepository
import VKApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class WaitingForStartHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Начать цикл") {
            async { clientsRepository.update(
                client.id,
                newStatus = Status.ACTIVE,
                newWeeksPassed = client.weeksPassed + 1
            ) }
            async { sendPlan(client) }
        } else {
            vkApiClient.sendMessage(
                client.id,
                "Для того, чтобы получить план и начать недельный цикл, нажмите \"Начать цикл\"."
            )
        }
    }

    private suspend fun sendPlan(client: Client) {
        val ids = client.trainingPlan.prepareAsAttachment(client.id)
        val phrases = listOf(
            "Хороших тренировок!",
            "Удачных тренировок!"
        )
        val phrase = if (client.trainingPlan.hours == 1)
            "Хорошего восстановления!"
        else
            phrases.random()
        vkApiClient.sendMessage(
            client.id,
            phrase,
            attachment = "doc${ids.first}_${ids.second}"
        )
    }
}