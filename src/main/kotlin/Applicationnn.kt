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

fun main(args: Array<String>): Unit = runBlocking {

    val clientsRepository = InDataBaseClientsRepository()
    clientsRepository.clear()
   // val client = clientsRepository.findById(217619042)!!
    val vkApiClient = VKApiClient()
    /*vkApiClient.sendMessage(217619042, "")

    vkApiClient.convertFileToAttachment("src/main/resources/TrainingPlans/8/10/0.pdf", client)
    vkApiClient.sendMessageSafely(217619042, "")*/
    //clientsRepository.clear()

    val trainingPlansRepository = TrainingPlansRepository(
        "src/main/resources/TrainingPlans"
    )
    val qiwiApiClient = QiwiApiClient()

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

    val clientsIterator = ClientIterator(
        clientsRepository,
        vkApiClient,
        qiwiApiClient
    )

    val context = newFixedThreadPoolContext(3, "for_iterator")

    launch(context) {
        clientsIterator.iterateOverClients(
            LocalTime.now().plusSeconds(5),
            Duration.ofSeconds(5)
        )
    }

    launch(Dispatchers.Main) {
        embeddedServer(Netty, port = 8080, configure = {
            runningLimit = 20
            shareWorkGroup = true
        }) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            routing(incomingMessageHandler, messageEventHandler)
        }.start(true)
    }




    /*
    launch(Dispatchers.Main) {
        EngineMain.main(args)
    }*/
}

/*
fun Application.module(testing: Boolean = false) {



    routing()
}
*/