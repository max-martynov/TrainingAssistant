import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime

val clientsRepository = InDataBaseClientsRepository()


@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {

    launch(newSingleThreadContext("Thread for iterators")) {
        iterateOverClients(
            LocalTime.now().plusSeconds(1),
            Duration.ofSeconds(5)
        )
    }
    launch {
        io.ktor.server.netty.EngineMain.main(args)
    }
}

fun Application.module(testing: Boolean = false) {

    clientsRepository.clear()

    install(ContentNegotiation) {
        json()
    }

    routing()
}