import api.vk.VKApiClient
import keyboards.MainKeyboardAfterPayment
import keyboards.MainKeyboardBeforePayment

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    clientsRepository.update(
        255408264,
        newStatus = Status.ACTIVE,
        newDaysPassed = 0,
        newWeeksPassed = 0
    )
    clientsRepository.getAll().forEach { println(it) }
}