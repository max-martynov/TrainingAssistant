package eventHandlers

import Client
import ClientsRepository
import Status
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.*
import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import keyboards.PressStartKeyboard
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import stateHandlers.*
import java.lang.management.ManagementFactory



class IncomingMessageHandler(
    private val clientsRepository: ClientsRepository,
    private val vKApiClient: VKApiClient,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val qiwiApiClient: QiwiApiClient
) {
    private val productId = 8 // 803 for Fake Community

//    private val newClientHandler = NewClientHandler(clientsRepository, vKApiClient)
//    private val waitingForPlanHandler = WaitingForPlanHandler(clientsRepository, trainingPlansRepository, vKApiClient)
//    private val waitingForStartHandler = WaitingForStartHandler(clientsRepository, vKApiClient)
//    private val activeClientHandler = ActiveClientHandler(clientsRepository, vKApiClient)
//    private val waitingForPaymentHandler = WaitingForPaymentHandler(clientsRepository, vKApiClient)
//    private val completingInterviewHandler0 = CompletingInterview0Handler(clientsRepository, trainingPlansRepository, vKApiClient, qiwiApiClient)
//    private val completingInterviewHandler1 = CompletingInterview1Handler(clientsRepository, trainingPlansRepository, vKApiClient, qiwiApiClient)
//    private val completingInterviewHandler2 = CompletingInterview2Handler(clientsRepository, trainingPlansRepository, vKApiClient, qiwiApiClient)
//    private val completingInterviewHandler3 = CompletingInterview3Handler(clientsRepository, trainingPlansRepository, vKApiClient, qiwiApiClient)

    suspend fun receiveMessage(incomingMessage: IncomingMessage) {
       /* val clientId = incomingMessage.fromId
        val text = incomingMessage.text
        val attachments = incomingMessage.attachments

        println("Current number of threads = ${ManagementFactory.getThreadMXBean().threadCount}")

        val client = clientsRepository.findById(clientId)

        if (client == null && attachments.isNotEmpty() && isOurProduct(attachments[0].toString())) {
            registerNewClient(clientId)
        }
        else if (client != null) {
            getAppropriateHandler(client).handle(client, text)
        }*/
    }

    /*private fun isOurProduct(attachment: String): Boolean {
        return Json { ignoreUnknownKeys = true }.decodeFromString<Attachment>(attachment).type == "market" &&
                Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<MarketAttachment>(attachment).market.category.id == productId
    }

    private suspend fun registerNewClient(clientId: Int): Unit = coroutineScope {
        async { clientsRepository.add(Client(clientId)) }
        async { sendGreetings(clientId) }
    }

    private suspend fun sendGreetings(peerId: Int) {
        vKApiClient.sendMessageSafely(
            peerId,
            "Привет!\n" +
                    "Я персональный тренер в Вашем смартфоне. Ознакомьтесь с принципом работы, нажав на кнопку \"Инструкция\". " +
                    "А после жмите \"Старт\", чтобы начать тренироваться вместо со мной!",
            keyboard = PressStartKeyboard().keyboard
        )
    }

    private fun getAppropriateHandler(client: Client): StateHandler {
        return when(client.status) {
            Status.NEW_CLIENT -> newClientHandler
            Status.WAITING_FOR_PLAN -> waitingForPlanHandler
            Status.WAITING_FOR_START -> waitingForStartHandler
            Status.ACTIVE -> activeClientHandler
            Status.WAITING_FOR_PAYMENT -> waitingForPaymentHandler
            Status.COMPLETING_INTERVIEW0 -> completingInterviewHandler0
            Status.COMPLETING_INTERVIEW1 -> completingInterviewHandler1
            Status.COMPLETING_INTERVIEW2 -> completingInterviewHandler2
            Status.COMPLETING_INTERVIEW3 -> completingInterviewHandler3
        }
    }*/
}



