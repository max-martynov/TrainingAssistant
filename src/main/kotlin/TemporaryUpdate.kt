import api.vk.VKApiClient
import keyboards.MainKeyboardAfterPayment
import keyboards.MainKeyboardBeforePayment

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    clientsRepository.update(
        143964633,
            newStatus = Status.WAITING_FOR_START,
            newWeeksPassed = 0,
            newDaysPassed = 0,
            newTrainingPlan = TrainingPlan(10, 10, 1),
            newInterviewResults = mutableListOf(),
    )
    vkApiClient.sendMessageSafely(
        143964633,
        "Оплата подтверждена! Спасибо, что решили продолжить тренировки по подписке."
    )
}