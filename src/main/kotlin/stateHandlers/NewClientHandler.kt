package stateHandlers

import Client
import ClientsRepository
import api.vk.VKApiClient
import keyboards.MainKeyboardBeforePayment
import keyboards.PrimaryActivityKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class NewClientHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Старт!") {
            sendMainKeyboardBeforePayment(client.id)
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

    private suspend fun sendMainKeyboardBeforePayment(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Отлично! Как и написано в инструкции, первые две недели Вы можете пользоваться моими услугами совершенно бесплатно, поэтому не переживайте, никакие денежные средства списываться не будут.\n" +
                    "Кратко об основных кнопках:\n" +
                    " 🔹 Обратная связь: при возникновении проблемы или вопроса, напишите об этом реальному человеку\n" +
                    " \uD83D\uDD39 Инструкция: с ней Вы уже знакомы\n" +
                    " \uD83D\uDD39 Видео: ссылка на видео с правильным выполнением упражнений\n" +
                    " \uD83D\uDD39 Начать / Закончить цикл: с ними Вы познакомитесь далее.",
            keyboard = MainKeyboardBeforePayment().keyboard
        )
    }

    private suspend fun sendSelectTrainingPlan(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Чтобы начать первую пробную неделю, выберите, как бы Вы хотели тренироваться. " +
                    "Сейчас Вам доступны 5 опций, начиная со следующей недели разблокируются еще 2 вида тренировок.\n" +
                    "Советы по выбору Вы можете найти в инструкции.",
            keyboard = PrimaryActivityKeyboard().keyboard
        )
    }
}


