package stateHandlers

import Client
import ClientsRepository
import api.vk.VKApiClient
import keyboards.MainKeyboardWithoutPromocodes
import keyboards.SelectHoursKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


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
            "Отлично! Для начала нужно выбрать нагруженность недельного цикла: пока что есть 2 опции - 6 или 10 часов:\n" +
                    "\uD83D\uDD38 Выбирайте 6 часов, если Вы хотите разнообразные и сбалансированные тренировки, которые помогут Вам улучшить выносливость, скоростные навыки и общую физическую форму\n" +
                    "\uD83D\uDD38 Выбирайте 10 часов, если Вы уверены в своих силах, планируете подготовиться к серьезным соревнованиям и просто хотите выйти на новый уровень",
            keyboard = MainKeyboardWithoutPromocodes().keyboard
        )
    }

    private suspend fun sendSelectTrainingPlan(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Сколько часов в неделю Вы хотели бы тренироваться? (для ответа просто нажмите на соответствующую синюю кнопку)",
            keyboard = SelectHoursKeyboard().keyboard
        )
    }
}


