package stateHandlers

import Client
import ClientsRepository
import api.vk.VKApiClient

abstract class StateHandler(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient
) {
    abstract suspend fun handle(client: Client, text: String)
}
