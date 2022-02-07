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


class CompletingInterview0Handler(
    private val clientsRepository: ClientsRepository,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val vkApiClient: VKApiClient,
    qiwiApiClient: QiwiApiClient
) : CompletingInterviewHandler(clientsRepository, vkApiClient, qiwiApiClient) {



    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (client.hasCompetition) {
            when (text) {
                "Да" -> {
                    async {
                        clientsRepository.update(
                            client.id,
                            newTrainingPlan = trainingPlansRepository.getTrainingPlan(client, 0, 0))
                    }
                    checkIfTrial(client)
                }
                "Нет" -> {
                    async {
                        clientsRepository.update(
                            client.id,
                            newTrainingPlan = TrainingPlan(0, 0, "")
                        )
                    }
                    async {
                        val questions = listOf(
                            "Как оцените Ваше состояние по окончании недельного цикла?",
                            "Как Вам дался этот цикл?"
                        )
                        vkApiClient.sendMessageSafely(
                            client.id,
                            questions.random(),
                            keyboard = HowWasPlanKeyboard().getKeyboard()
                        )
                    }
                }
            }
        }
        else {
            val recommendation = TextAnalyzer.processText(client, text, vkApiClient) ?: return@coroutineScope
            async {
                clientsRepository.update(
                    client.id,
                    newStatus = Status.COMPLETING_INTERVIEW4
                )
            }
            async {
                vkApiClient.sendMessageSafely(
                    client.id,
                    "$recommendation\nВыбор за Вами:",
                    keyboard = MainActivityKeyboard().getKeyboard()
                )
            }
        }
    }
}