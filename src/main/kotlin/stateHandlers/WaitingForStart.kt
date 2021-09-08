package stateHandlers

import Client
import ClientsRepository
import TrainingPlansRepository
import api.vk.VKApiClient
import io.ktor.client.features.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.time.LocalDateTime


class WaitingForStartHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
    private val trainingPlansRepository: TrainingPlansRepository
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
            vkApiClient.sendMessageSafely(
                client.id,
                "Для того, чтобы получить план и начать недельный цикл, нажмите \"Начать цикл\"."
            )
        }
    }

    private suspend fun sendPlan(client: Client) {
        val phrases = listOf(
            "Хороших тренировок!",
            "Удачных тренировок!"
        )
        val phrase = if (client.trainingPlan.hours == 1)
            "Хорошего восстановления!"
        else
            phrases.random()
        var attachment = ""
        try {
            attachment = vkApiClient.convertFileToAttachment(
                trainingPlansRepository.getPathToFile(client.trainingPlan),
                client
            )
        } catch (e: ResponseException) {
            println("${LocalDateTime.now()}: Exception while loading a document event answer - ${e.message}\nRetrying...")
            delay(1000L)
            try {
                attachment = vkApiClient.convertFileToAttachment(
                    trainingPlansRepository.getPathToFile(client.trainingPlan),
                    client
                )
            } catch (e: ResponseException) {
                println("${LocalDateTime.now()}: Still exception - ${e.message}\n")
            }
        }
        vkApiClient.sendMessageSafely(
            client.id,
            phrase,
            attachment = attachment
        )
    }
}