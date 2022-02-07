package stateHandlers

import client.Client
import ClientsRepository
import api.vk.VKApiClient
import client.Status
import keyboards.AlrightKeyboard
import keyboards.MainKeyboardBeforePayment
import keyboards.PrimaryActivityKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope


class NewClientHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler() {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Старт!") {
            sendMainKeyboardBeforePayment(client.id)
        }
        else if (text == "Все понятно!") {
            async { sendSelectTrainingPlan(client.id) }
            async { clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_PLAN
            ) }
        }
    }

    private suspend fun sendMainKeyboardBeforePayment(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Отлично! Давайте разберемся с основными кнопками:\n" +
                    " 🔹 Инструкция: прочитайте, чтобы ознакомиться со всеми моими возможностями\n" +
                    " 🔹 Написать тренеру: пообщайтесь с реальным человеком, если у Вас возникнут вопросы\n" +
                    " 🔹 Видео: статья с ссылками на видео с правильным выполнением упражнений\n" +
                    "Важно: эти кнопки всегда можно свернуть и развернуть, нажав на значок на краю поля для ввода сообщения!",
            keyboard = MainKeyboardBeforePayment().getKeyboard()
        )
        vkApiClient.sendMessageSafely(
            peerId,
            "Иногда также надо будет нажимать на кнопку прямо внутри сообщения. " +
                    "Например, сейчас нажмите на зеленую кнопку ниже, если Вы все поняли и готовы продолжить:",
            keyboard = AlrightKeyboard().getKeyboard()
        )
    }

    private suspend fun sendSelectTrainingPlan(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "Пришло время определиться с Вашим первым тренировочным планом!\n" +
                    "Сейчас Вам доступны 5 опций, начиная со следующей недели, разблокируются еще 2 вида тренировок.\n" +
                    "Советы по выбору Вы вседа можете найти в инструкции.\n" +
                    "Чтобы выбрать тренировочное направление, просто нажмите на соотвествующую синюю кнопку.",
            keyboard = PrimaryActivityKeyboard().getKeyboard()
        )
    }
}


