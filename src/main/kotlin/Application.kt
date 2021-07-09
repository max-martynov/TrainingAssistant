import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime

val clientsRepository = InMemoryClientsRepository()
val trainingPlansRepository = TrainingPlansRepository(
    "src/main/resources/TrainingPlans",
    2
)


@OptIn(ObsoleteCoroutinesApi::class)
fun main(args: Array<String>): Unit = runBlocking {
    //val applicationEnvironment = commandLineEnvironment(args)
    //clientRepository = loadClientRepository(applicationEnvironment.config)

    //clientRepository = InMemoryClientRepository()

    //trainingPlansRepository = LocalTrainingPlansRepository()

    launch(newSingleThreadContext("Thread for iterators")) {
        iterateOverClients(
            LocalTime.now().plusSeconds(1),
            Duration.ofSeconds(1)
        )
    /*val clientsIterator = ClientsIterator(
            clientRepository,
            LocalTime.now().plusSeconds(9),
            LocalTime.now().plusSeconds(20),
            LocalTime.now().plusSeconds(24),
            Duration.ofSeconds(24)
        )*/
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