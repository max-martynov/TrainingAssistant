import api.vk.VKApiClient
import keyboards.MainKeyboardAfterPayment
import keyboards.MainKeyboardBeforePayment

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    clientsRepository.update(
        23661247,
        newDaysPassed = 22,
        newTrainingPlan = TrainingPlan(11, 10, 4)
    )
    clientsRepository.update(
        255408264,
        newDaysPassed = 10,
        newTrainingPlan = TrainingPlan(11, 6, 4)
    )
    clientsRepository.update(
        81087718,
        newDaysPassed = 23,
        newTrainingPlan = TrainingPlan(11, 6, 4)
    )
    clientsRepository.update(
        464281827,
        newDaysPassed = 25,
        newTrainingPlan = TrainingPlan(11, 6, 4)
    )
    clientsRepository.update(
        166138003,
        newDaysPassed = 16,
        newTrainingPlan = TrainingPlan(11, 10, 4)
    )
    clientsRepository.update(
        247100783,
        newDaysPassed = 17,
        newStatus = Status.ACTIVE,
        newTrainingPlan = TrainingPlan(11, 10, 4)
    )
    clientsRepository.update(
        143964633,
        newDaysPassed = 14
    )
    clientsRepository.getAll().forEach { println(it) }
}