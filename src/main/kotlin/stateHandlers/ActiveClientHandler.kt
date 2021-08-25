package stateHandlers

import Client
import ClientsRepository
import VKApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ActiveClientHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Закончить цикл") {
            val phrases = listOf(
                "Поздравляю с окончанием недельного цикла!\nЧтобы сформировать план на следующую неделю, пройдите, пожалуйста, небольшой опрос.",
                "Отличная работа!\nДля формирования плана на следующую неделю, пройдите, пожалуйста, небольшой опрос.",
                "Недельный цикл успешно завершен! Пройдите, пожалуйста, небольшой опрос, чтобы сформировать план на следующую неделю."
            )
            vkApiClient.sendMessage(
                client.id,
                if (client.trial)
                    "Поздравляю с окончанием пробной недели!\nДля формирования следующего плана, пройдите, пожалуйста, небольшой опрос."
                else
                    phrases.random()
            )
            async { clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_RESULTS
            ) }
            async { sendInterviewQuestion(client, 0) }
        } else {
            vkApiClient.sendMessage(
                client.id,
                "Для того, чтобы закончить выполнение недельного цикла, нажмите \"Закончить цикл\"."
            )
        }
    }

    private suspend fun sendInterviewQuestion(client: Client, questionNumber: Int) {
        vkApiClient.sendMessage(
            client.id,
            client.interview.interviewQuestions[questionNumber].question,
            keyboard = client.interview.interviewQuestions[questionNumber].toString()
        )
    }
}
