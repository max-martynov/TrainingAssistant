package stateHandlers

import client.Client
import ClientsRepository
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import kotlinx.coroutines.coroutineScope


class CompletingInterview1Handler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient,
    qiwiApiClient: QiwiApiClient
) : CompletingInterviewHandler(clientsRepository, vkApiClient, qiwiApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        when (text) {
            "6 часов" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 0, 0)
                )
                checkIfTrial(client)
            }
            "10 часов" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 0, 1)
                )
                checkIfTrial(client)
            }
            else -> {
                vkApiClient.sendMessageSafely(
                    client.id,
                    "Выберите, пожалуйста, один из предложенных вариантов ответа.  Если у Вас возникли вопросы, нажмите \"Обратная связь\"."
                )
            }
        }
    }
}