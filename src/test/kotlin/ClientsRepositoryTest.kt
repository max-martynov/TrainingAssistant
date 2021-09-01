import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
/*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientsRepositoryTest {

    private val clientsRepository = InDataBaseClientsRepository()
    //private val clientsRepository = InMemoryClientsRepository()

    private val clients = listOf(
        Client(id = 0),
        Client(
            id = 1,
            status = Status.ACTIVE
        ),
        Client(
            id = 2,
            trial = true,
            status = Status.WAITING_FOR_PAYMENT,
            daysPassed = 20,
            weeksPassed = 2,
            trainingPlan = TrainingPlan(1, 6, 2),
            interviewResults = mutableListOf(0, 1, 2)
        )
    )


    @BeforeAll
    fun addDefaultClients() = runBlocking {
        clientsRepository.clear()
        clients.forEach {
            clientsRepository.add(it)
        }
    }

    @AfterAll
    fun clearRepository() {
        clientsRepository.clear()
    }

    @Test
    fun `getAll from repository`() = runBlocking {
        assertEquals(clients, clientsRepository.getAll())
    }

    @Test
    fun `add already existed clients`() = runBlocking {
        clientsRepository.add(Client(id = 2))
        assertEquals(clients, clientsRepository.getAll())
    }

    @Test
    fun `findById in repository`() = runBlocking {
        clients.forEach {
            assertEquals(it, clientsRepository.findById(it.id))
        }
        assertEquals(null, clientsRepository.findById(100500))
    }

    @Test
    fun `update clients`() = runBlocking {
        clientsRepository.update(
            id  = 0,
            newStatus = Status.ACTIVE
        )
        assertEquals(Status.ACTIVE, clientsRepository.findById(0)?.status)
        assertEquals(Status.WAITING_FOR_PLAN, clientsRepository.findById(0)?.previousStatus)

        clientsRepository.update(
            id = 1,
            newTrial = false,
            newDaysPassed = 31,
            newWeeksPassed = 3,
            newInterviewResults = mutableListOf(0, 1),
            newTrainingPlan = TrainingPlan(10, 11, 12)
        )
        assertEquals(false, clientsRepository.findById(1)?.trial)
        assertEquals(31, clientsRepository.findById(1)?.daysPassed)
        assertEquals(3, clientsRepository.findById(1)?.weeksPassed)
        assertEquals(mutableListOf(0, 1), clientsRepository.findById(1)?.interviewResults)
        assertEquals(10, clientsRepository.findById(1)?.trainingPlan?.month)
        assertEquals(11, clientsRepository.findById(1)?.trainingPlan?.hours)
        assertEquals(12, clientsRepository.findById(1)?.trainingPlan?.week)
    }
}*/