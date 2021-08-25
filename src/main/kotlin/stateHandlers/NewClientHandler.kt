package stateHandlers

import Client
import ClientsRepository
import TrainingPlan
import VKApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mainKeyboardWithoutPromocodes
import selectHoursKeyboard
import java.time.LocalDate

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
            vkApiClient.sendMessage(
                client.id,
                "Если вы готовы начать, жмите \"Старт!\""
            )
        }
    }

    private suspend fun sendMainKeyboardWithoutPromocodes(peerId: Int) {
        vkApiClient.sendMessage(
            peerId,
            "Отлично! Для начала нужно выбрать нагруженность недельного цикла: пока что есть 2 опции - 6 или 10 часов.",
            keyboard = mainKeyboardWithoutPromocodes
        )
    }

    private suspend fun sendSelectTrainingPlan(peerId: Int) {
        vkApiClient.sendMessage(
            peerId,
            "Сколько часов в неделю у Вас есть возможность тренироваться?",
            keyboard = selectHoursKeyboard
        )
    }
}


