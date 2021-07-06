import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

lateinit var clientRepository: ClientRepository
lateinit var trainingPlansRepository: TrainingPlansRepository


@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {
    //val applicationEnvironment = commandLineEnvironment(args)
    //clientRepository = loadClientRepository(applicationEnvironment.config)

    clientRepository = InMemoryClientRepository()
    trainingPlansRepository = LocalTrainingPlansRepository()

    launch(newSingleThreadContext("Thread for iterators")) {
        val clientsIterator = ClientsIterator(
            clientRepository,
            LocalTime.now().plusSeconds(9),
            LocalTime.now().plusSeconds(20),
            LocalTime.now().plusSeconds(24),
            Duration.ofSeconds(24)
        )
        //async { clientsIterator.iterateMorning() }
        //async { clientsIterator.iterateEvening() }
        //async { clientsIterator.iterateNight() }
    }
    launch {
        io.ktor.server.netty.EngineMain.main(args)
    }
}

fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        json()
    }

    routing()
}