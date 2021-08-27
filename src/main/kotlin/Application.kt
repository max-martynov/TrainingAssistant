import ApiClients.VKApiClient
import com.petersamokhin.vksdk.core.client.VkApiClient
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import kotlinx.coroutines.repackaged.net.bytebuddy.build.Plugin
import kotlinx.serialization.json.Json
import routing
import java.io.InputStream
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.concurrent.thread

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

    val paymentChecker = PaymentChecker(
        clientsRepository,
        vkApiClient,
        trainingPlansRepository,
        qiwiApiClient
    )

    val incomingMessageHandler = IncomingMessageHandler(
        clientsRepository,
        vkApiClient,
        trainingPlansRepository,
        paymentChecker
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
        /*routing {
            post("/") {
                withContext(Dispatchers.IO) {
                    call.receive<InputStream>().use {
                        val notification = it.readBytes().decodeToString()
                        when (getType(notification)) {
                            "message_new" -> {
                                call.respondText("ok")
                               incomingMessageHandler.receiveMessage(notification)
                            }
                            /*"vkpay_transaction" -> {
                                call.respondText("ok")
                                receivePayment(notification)
                            }*/
                            "message_event" -> {
                                call.respondText("ok")
                                paymentChecker.checkPayment(notification)
                            }
                            "confirmation" -> {
                                val responseString = "be0eac70" // Warning! May change after some time
                                call.respondText(responseString)
                            }
                            else -> call.respondText("ok")
                        }
                    }
                }
            }
        }*/
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