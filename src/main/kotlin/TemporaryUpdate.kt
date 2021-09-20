import api.vk.VKApiClient
import keyboards.MainKeyboardAfterPayment
import keyboards.MainKeyboardBeforePayment

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    val text = "Большое обновление: новые фразы, раздел с мотивацией и оценка тренировочных планов!\n\n" +
            "Новые фразы 🔥\nТеперь чат-бот стал отвечать понятнее и разнообразнее. Это делает взаимодействие с ним проще и интереснее!\n\n" +
            "Раздел с мотивацией 🏆\nК разделу с эксклюзивными промокодами теперь добавилась статья, в которой собраны лучшее мотивирующие фильмы. Доступно после первой оплаты.\n\n" +
            "Возможность оценивать тренировочные планы 🤩\nПо окончании тренировочного цикла Вам будет предложено оценить его по 5-бальной шкале. Все Ваши оценки будут учитываться при формировании следующих планов.\n\n" +
            "Опробуйте все это в действии, если Вы уже тренируетесь с нами! А если Вы остановились на пробной неделе, то не бойтесь идти дальше!"
    clientsRepository.getAll().forEach {
        vkApiClient.sendMessageSafely(
            it.id,
            text,
            keyboard = if (!it.trial) MainKeyboardAfterPayment().keyboard else MainKeyboardBeforePayment().keyboard
        )
    }
}