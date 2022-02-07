package stateHandlers

import client.Client
import ClientsRepository
import client.Status
import TrainingPlan
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import keyboards.HowWasPlanKeyboard
import keyboards.MainActivityKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class CompletingInterview4Handler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient,
    qiwiApiClient: QiwiApiClient
) : CompletingInterviewHandler(clientsRepository, vkApiClient, qiwiApiClient) {


    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        when (text) {
            "Лыжи" -> {
                async {
                    clientsRepository.update(
                        client.id,
                        newStatus = Status.COMPLETING_INTERVIEW1,
                    )
                }
                async {
                    askHours(client)
                }
            }
            "Бег" -> {
                async {
                    clientsRepository.update(
                        client.id,
                        newStatus = Status.COMPLETING_INTERVIEW2
                    )
                }
                async {
                    askHours(client)
                }
            }
            "ОФП" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 2, 0)
                )
                checkIfTrial(client)
            }
            "Восстановление" -> {
                clientsRepository.update(
                    client.id,
                    newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 3, 0)
                )
                checkIfTrial(client)
            }
            "Подводка к старту" -> {
                async {
                    clientsRepository.update(
                        client.id,
                        newStatus = Status.COMPLETING_INTERVIEW3
                    )
                    if (client.trainingPlan.duration > 1) {
                        clientsRepository.update(
                            client.id,
                            newTrainingPlan = TrainingPlan(
                                client.trainingPlan.activityType,
                                0,
                                ""
                            )
                        )
                    }
                }
                async {
                    askDay(client)
                }
            }
            else -> {
                vkApiClient.sendMessageSafely(
                    client.id,
                    "Выберите, пожалуйста, один из предложенных вариантов ответа. Если у Вас возникли вопросы, нажмите \"Обратная связь\"."
                )
            }
        }
    }
}
