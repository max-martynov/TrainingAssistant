package stateHandlers

import Client
import ClientsRepository
import TrainingPlan
import api.vk.VKApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class WaitingForPlanHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler(clientsRepository, vkApiClient) {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "6 часов" || text == "10 часов") {
            async { clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_START,
                newTrainingPlan = TrainingPlan(
                    LocalDate.now().monthValue,
                    if (text == "6 часов") 6 else 10,
                    0
                )
            ) }
            async { sendTrialMessage(client.id) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Выберите, пожалуйста, сколько часов в неделю Вы хотите тренироваться."
            )
        }
    }

    private suspend fun sendTrialMessage(peerId: Int) {
        vkApiClient.sendMessageSafely(
            peerId,
            "У нас для Вас хорошие новости! Чтобы Вы попробовали обновленные тренировки по подписке, не рискую своими деньгами, первая неделя в подарок 🎁\n" +
                    "Пройдите недельный план и, если Вам понравится, оформите подписку на целый месяц персональных тренировок!\n" +
                    "Чтобы получить план и начать недельный цикл, нажмите \"Начать цикл\" (если Вы не видите этой кнопки, нажмите на кнопку чуть правее поля для ввода сообщения)"
        )
    }
}