import eventHandlers.IncomingMessageHandler
import eventHandlers.MessageEventHandler
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import client.ClientIterator
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import io.ktor.server.jetty.Jetty
import java.io.File
import java.io.InputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime


//@OptIn(ObsoleteCoroutinesApi::class)
fun main(): Unit = runBlocking {
    val clientsRepository = InDataBaseClientsRepository()
    clientsRepository.clear()
    val trainingPlansRepository = TrainingPlansRepository()
    val vkApiClient = VKApiClient()
    val qiwiApiClient = QiwiApiClient()

    temporaryUpdate(clientsRepository, vkApiClient)

    val context = newFixedThreadPoolContext(3, "for_iterator")

    launch(context) {
        val clientsIterator = ClientIterator(
            clientsRepository,
            vkApiClient,
            qiwiApiClient
        )
        clientsIterator.iterateOverClients(
            LocalTime.now().plusSeconds(5), // For testing only!
            Duration.ofSeconds(60)
        )
    }

    launch(Dispatchers.Main) {
        val messageEventHandler = MessageEventHandler(
            clientsRepository,
            vkApiClient,
            qiwiApiClient
        )

        val incomingMessageHandler = IncomingMessageHandler(
            clientsRepository,
            vkApiClient,
            trainingPlansRepository,
            qiwiApiClient
        )

        embeddedServer(Jetty, port = 8080, configure = {
            connectionGroupSize = 2
            workerGroupSize = 5
            callGroupSize = 10
        }) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing(incomingMessageHandler, messageEventHandler)
        }.start(true)
    }
}
