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
            LocalTime.now().plusSeconds(15),
            LocalTime.now().plusSeconds(30),
            Duration.ofSeconds(30)
        )
        async { clientsIterator.iterateMorning() }
        async { clientsIterator.iterateEvening() }
    }
    launch {
        io.ktor.server.netty.EngineMain.main(args)
    }
}

//@OptIn(ObsoleteCoroutinesApi::class)
fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        json()
    }

    /*clientRepository.addClient(
        Client(
            id = 217619042,
            status = Status.ACTIVE,
            daysInWeekPassed = 0,
            totalDaysPassed = 0,
            trainingPlan = trainingPlansRepository.findTrainingPlan(2)!!,
            interviewResults = mutableListOf()
        )
    )*/

    routing()
    // }

    /* Database.connect("jdbc:h2:~/test", driver = "org.h2.Driver")

     transaction {
         addLogger(StdOutSqlLogger)

         SchemaUtils.create (Clients2)

         Clients2.insert { it[id] = 217619041 }

         Clients2.select { Clients.vkId eq 217619041 }.forEach {

         }


     }*/


    //val res = clientQueries.findClientById(217619042).executeAsOne().toInt()
    //println(res)
    //dataBase.addClient(Client(217619042))


}