package stateHandlers

import client.Client
import ClientsRepository
import api.vk.VKApiClient
import client.Status
import keyboards.GoToPlanKeyboard
import keyboards.HowWasPlanKeyboard
import keyboards.MainActivityKeyboard
import keyboards.YesNoKeyboard
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ActiveClientHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) : StateHandler() {

    override suspend fun handle(client: Client, text: String): Unit = coroutineScope {
        if (text == "Закончить цикл") {
            val phrasesPart1 = listOf(
                "Отлично 🔥\n",
                "Хорошая работа 💪\n",
                "Недельный цикл успешно завершен 😎\n"
            )
            val phrasesPart2 = listOf(
                "Если Вы хотите оставить отзыв о проделанной работе или задать любой вопрос о тренировках, воспользуйтесь кнопкой \"Написать тренеру\".\n",
                "Возможно, у Вас есть вопрос по поводу проделанных тренировок? Не бойтесь задать его реальному человеку! Для этого просто нажмите кнопку \"Написать тренеру\".\n",
                "Если недельный план Вас чем-то не утроил или у Вас есть иной вопрос по поводу тренировочного процесса, воспользуйтеся кнопкой \"Написать тренеру\".\n"
            )
            val phrasesPart3 = listOf(
                "Чтобы обпределиться с планом на следующую неделю, нажмите зеленую кнопку ниже.",
                "А чтобы выбрать новый план и продолжить тренировки, нажмите зеленую кнопку ниже."
            )
            vkApiClient.sendMessageSafely(
                client.id,
                phrasesPart1.random() + phrasesPart2.random() + phrasesPart3.random(),
                keyboard = GoToPlanKeyboard().getKeyboard()
            )
        } else if (text == "Перейти к выбору плана") {
            async { clientsRepository.update(
                client.id,
                newStatus = Status.COMPLETING_INTERVIEW0,
                newWeeksPassed = client.weeksPassed + 1
            ) }
            async { sendFirstQuestion(client) }
        } else {
            vkApiClient.sendMessageSafely(
                client.id,
                "Для того, чтобы закончить выполнение недельного цикла, нажмите кнопку \"Закончить цикл\"."
            )
        }
    }

    private suspend fun sendFirstQuestion(client: Client) {
        val questions = listOf(
            "Как оцените Ваше состояние по окончании недельного цикла?",
            "Как Вам дался этот цикл?"
        )
        vkApiClient.sendMessageSafely(
            client.id,
            questions.random(),
            keyboard = HowWasPlanKeyboard().getKeyboard()
        )
    //        if (client.hasCompetition) {
//            vkApiClient.sendMessageSafely(
//                client.id,
//                "Пробежали ли Вы старт?",
//                keyboard = YesNoKeyboard().getKeyboard()
//            )
//        }
//        else {
//            vkApiClient.sendMessageSafely(
//                client.id,
//                "Выберите, как бы Вы хотели тренироваться на этой неделе. Подробнее о типах тренировок, а также советы по выбору, Вы всегда можете найти в инструкции.\n" +
//                        "Чтобы выбрать тренировочное направление, просто нажмите на соотвествующую синюю кнопку.",
//                keyboard = MainActivityKeyboard().getKeyboard()
//            )
//        }
    }
}
