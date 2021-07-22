import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime

val clientsRepository = InDataBaseClientsRepository()
const val accessToken = "8d8088feeb18744bc2e5a7ed11067faf9cf495fce1c99c6c430e59b7e093f6a45ff827bc0333dd1bd2172"

/**
 * FakeCommunity - "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e"
 * Prosto - "8d8088feeb18744bc2e5a7ed11067faf9cf495fce1c99c6c430e59b7e093f6a45ff827bc0333dd1bd2172"
 */


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
