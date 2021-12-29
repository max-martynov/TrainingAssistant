package stateHandlers

import client.Client
import ClientsRepository
import TrainingPlan
import TrainingPlansRepository
import api.vk.VKApiClient
import client.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class WaitingForPlanHandler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient
) : StateHandler() {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        val trainingPlan: TrainingPlan? = when (text) {
            "Лыжи, 6 часов в неделю" -> trainingPlansRepository.getTrainingPlan(client, 0, 0)
            "Лыжи, 10 часов в неделю" -> trainingPlansRepository.getTrainingPlan(client, 0, 1)
            "Бег, 6 часов в неделю" -> trainingPlansRepository.getTrainingPlan(client, 1, 0)
            "Бег, 10 часов в неделю" -> trainingPlansRepository.getTrainingPlan(client, 1, 1)
            "ОФП, 4 часа в неделю" -> trainingPlansRepository.getTrainingPlan(client,2, 0)
            else -> null
        }
        if (trainingPlan != null) {
            async { clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_START,
                newTrainingPlan = trainingPlan
            ) }
            async { sendTrialMessage(client.id) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Выберите, пожалуйста, как бы Вы хотели тренироваться. Если у Вас возникли вопросы, нажмите \"Обратная связь\"."
            )
        }
    }

    private suspend fun sendTrialMessage(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Недельный план сформирован!\n" +
                    "Чтобы получить его и начать недельный цикл, нажмите кнопку \"Начать цикл\". " +
                    "Через неделю, после того, как выполните все тренировки, нажмите кнопку \"Закончить цикл\"."
        )
    }
}