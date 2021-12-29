package stateHandlers

import client.Client
import ClientsRepository
import api.vk.VKApiClient

abstract class StateHandler() {
    abstract suspend fun handle(client: Client, text: String)
}
