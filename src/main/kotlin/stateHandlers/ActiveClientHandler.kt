package stateHandlers

import Client
import ClientsRepository
import api.vk.VKApiClient
import keyboards.HasCompetitionKeyboard
import keyboards.MainActivityKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ActiveClientHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Закончить цикл") {
            val phrases = listOf(
                "Отлично 🔥\nДавайте сформируем план на следующую неделю!",
                "Хорошая работа 💪\nДавайте определимся с тренировочным планом на следующую неделю!",
                "Недельный цикл успешно завершен 😎\nТеперь необходимо сформировать план на следующую неделю!"
            )
            vkApiClient.sendMessageSafely(
                client.id,
                phrases.random()
            )
            async { clientsRepository.update(
                client.id,
                newStatus = Status.COMPLETING_INTERVIEW0,
                newWeeksPassed = client.weeksPassed + 1
            ) }
            async { sendFirstQuestion(client) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Для того, чтобы закончить выполнение недельного цикла, нажмите \"Закончить цикл\". Если у Вас возникли вопросы, нажмите \"Обратная связь\"."
            )
        }
    }

    private suspend fun sendFirstQuestion(client: Client) {
        if (client.hasCompetition) {
            vkApiClient.sendMessageSafely(
                client.id,
                "Пробежали ли Вы старт?",
                keyboard = HasCompetitionKeyboard().keyboard
            )
        }
        vkApiClient.sendMessageSafely(
            client.id,
            "Выберите, как бы Вы хотели тренироваться на этой неделе. Подробнее о типах тренировок, а также советы по выбору, Вы всегда можете найти в инструкции.",
            keyboard = MainActivityKeyboard().keyboard
        )
    }
}
