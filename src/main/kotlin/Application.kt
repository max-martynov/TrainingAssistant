import io.ktor.application.*
import io.ktor.client.*
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
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.LocalTime
import kotlin.concurrent.thread

val clientsRepository: ClientsRepository = InDataBaseClientsRepository()
const val productId = 8 // 803 for Fake Community
const val groupId = 136349636 // 205462754 for Fake Community
const val paymentAmount = 1
const val startFromAugust = true


fun main(args: Array<String>): Unit = runBlocking {

    val context = newFixedThreadPoolContext(3, "for_iterator")

    launch(context) {
        iterateOverClients(
            LocalTime.now().plusSeconds(5),
            Duration.ofSeconds(5)
        )
    }

    launch(Dispatchers.Main) {
        EngineMain.main(args)
    }
}

fun Application.module(testing: Boolean = false) {

    clientsRepository.clear()

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }

    routing()
}
