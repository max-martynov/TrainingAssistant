import eventHandlers.IncomingMessageHandler
import eventHandlers.MessageEventHandler
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import client.ClientIterator
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalTime


@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {
    val clientsRepository = InDataBaseClientsRepository()
    val trainingPlansRepository = TrainingPlansRepository(
        "src/main/resources/TrainingPlans"
    )
    val vkApiClient = VKApiClient()
    val qiwiApiClient = QiwiApiClient()

    val context = newFixedThreadPoolContext(3, "for_iterator")

    launch(context) {
        val clientsIterator = ClientIterator(
            clientsRepository,
            vkApiClient,
            qiwiApiClient
        )
        clientsIterator.iterateOverClients(
            //LocalTime.now().plusSeconds(5),
            //Duration.ofSeconds(10)
        )
    }

    launch(Dispatchers.Main) {
        val messageEventHandler = MessageEventHandler(
            clientsRepository,
            vkApiClient,
            trainingPlansRepository,
            qiwiApiClient
        )

        val incomingMessageHandler = IncomingMessageHandler(
            clientsRepository,
            vkApiClient,
            trainingPlansRepository,
            qiwiApiClient
        )

        embeddedServer(Netty, port = 8080, configure = {
            runningLimit = 20
            shareWorkGroup = true
        }) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            //install(CallLogging)
            routing(incomingMessageHandler, messageEventHandler)
        }.start(true)
    }
}
