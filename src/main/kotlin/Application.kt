import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime

val clientsRepository = InDataBaseClientsRepository()
const val accessToken = "966e06e54eb46a92110ef0afafa083888c8abc2c272504fa1b94fbe22de8193e37224c8daa8cc496cff2f"

/**
 * FakeCommunity - "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e"
 * FakeCommunity2.0 - "966e06e54eb46a92110ef0afafa083888c8abc2c272504fa1b94fbe22de8193e37224c8daa8cc496cff2f"
 */


@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {

    /*launch(newSingleThreadContext("Thread for iterators")) {
        iterateOverClients(
            LocalTime.now().plusSeconds(1),
            Duration.ofSeconds(5)
        )
    }*/
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