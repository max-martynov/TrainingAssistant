import api.vk.VKApiClient
import keyboards.MainKeyboardAfterPayment
import keyboards.MainKeyboardBeforePayment

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    clientsRepository.update(
        464281827,
        newWeeksPassed = 1
    )
}