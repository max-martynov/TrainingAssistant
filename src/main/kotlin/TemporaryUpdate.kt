import api.vk.VKApiClient

suspend fun temporaryUpdate(clientsRepository: ClientsRepository, vkApiClient: VKApiClient) {
    clientsRepository.getAll().forEach { println(it) }
}