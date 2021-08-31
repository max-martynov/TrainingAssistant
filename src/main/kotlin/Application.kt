import eventHandlers.IncomingMessageHandler
import eventHandlers.MessageEventHandler
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

//val clientsRepository: ClientsRepository = InDataBaseClientsRepository()
const val productId = 8 // 803 for Fake Community
const val paymentAmount = 500

fun main(args: Array<String>): Unit = runBlocking {

    //    clientsRepository.clear()
    //clientsRepository.delete(217619042)
    //clientsRepository.delete(15733972)
    //val client = clientsRepository.findById(247100783)!!
    //client.updateBill()
    //requestPaymentToStart(client)

    //sendMessage(1625899520, "Тест")

    val context = newFixedThreadPoolContext(3, "for_iterator")

    val clientsRepository = InDataBaseClientsRepository()
    val vkApiClient = VKApiClient()
    val trainingPlansRepository = TrainingPlansRepository(
        "src/main/resources/TrainingPlans"
    )
    val qiwiApiClient = QiwiApiClient()

    val paymentChecker = MessageEventHandler(
        clientsRepository,
        vkApiClient,
        trainingPlansRepository,
        qiwiApiClient
    )

    val incomingMessageHandler = IncomingMessageHandler(
        clientsRepository,
        vkApiClient,
        trainingPlansRepository
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
        routing(incomingMessageHandler, paymentChecker)
    }.start(true)

/*    launch(context) {
        iterateOverClients(
          //  LocalTime.now().plusSeconds(5),
          //  Duration.ofSeconds(5)
        )
    }*/

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