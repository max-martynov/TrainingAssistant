package stateHandlers

import client.Client
import ClientsRepository
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient


class CompletingInterview3Handler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient,
    qiwiApiClient: QiwiApiClient
) : CompletingInterviewHandler(clientsRepository, vkApiClient, qiwiApiClient) {

    override suspend fun handle(client: Client, text: String) {
        when (text) {
            "Суббота" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 4, client.trainingPlan.duration)
                )
                checkIfTrial(client)
            }
            "Воскресенье" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 4, 10 + client.trainingPlan.duration)
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