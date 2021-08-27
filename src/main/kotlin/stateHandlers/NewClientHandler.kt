package stateHandlers

import Client
import ClientsRepository
import ApiClients.VKApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mainKeyboardWithoutPromocodes
import selectHoursKeyboard

class NewClientHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Старт!") {
            sendMainKeyboardWithoutPromocodes(client.id)
            async { sendSelectTrainingPlan(client.id) }
            async { clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_PLAN
            ) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Если вы готовы начать, жмите \"Старт!\""
            )
        }
    }

    private suspend fun sendMainKeyboardWithoutPromocodes(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Отлично! Для начала нужно выбрать нагруженность недельного цикла: пока что есть 2 опции - 6 или 10 часов.",
            keyboard = mainKeyboardWithoutPromocodes
        )
    }

    private suspend fun sendSelectTrainingPlan(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Сколько часов в неделю у Вас есть возможность тренироваться?",
            keyboard = selectHoursKeyboard
        )
    }
}


