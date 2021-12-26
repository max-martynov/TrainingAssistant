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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

const val pathToTrainingPlansRepository = "src/main/resources/TrainingPlans"

//@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {
    val clientsRepository = InDataBaseClientsRepository()
    clientsRepository.clear()
    val trainingPlansRepository = TrainingPlansRepository(
        pathToTrainingPlansRepository
    )
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
            Duration.ofSeconds(10)
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
            //install(CallLogging)
            routing(incomingMessageHandler, messageEventHandler)
        }.start(true)
    }
}
