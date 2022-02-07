package stateHandlers

import client.Client
import ClientsRepository
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient


class CompletingInterview2Handler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient,
    qiwiApiClient: QiwiApiClient
) : CompletingInterviewHandler(clientsRepository, vkApiClient, qiwiApiClient) {

    override suspend fun handle(client: Client, text: String) {
        when (text) {
            "6 часов" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 1, 0)
                )
                checkIfTrial(client)
            }
            "10 часов" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 1, 1)
                )
                checkIfTrial(client)
            }
            else -> {
                vkApiClient.sendMessageSafely(
                    client.id,
                    "Выберите, пожалуйста, один из предложенных вариантов ответа."
                )
            }
        }
    }
}