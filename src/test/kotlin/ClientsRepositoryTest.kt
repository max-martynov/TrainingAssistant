import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientsRepositoryTest {

    private val clientsRepository = InDataBaseClientsRepository()

    private val clients = listOf(
        Client(id = 0),
        Client(
            id = 1,
            status = Status.ACTIVE
        ),
        Client(
            id = 2,
            status = Status.WAITING_FOR_PAYMENT,
            daysPassed = 20,
            trainingPlanId = 10,
            interviewResults = mutableListOf(0, 1, 2)
        )
    )


    @BeforeAll
    fun addDefaultClients() = runBlocking {
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
            newDaysPassed = 31,
            newInterviewResults = mutableListOf(0, 1),
            newTrainingPlanId = 1
        )
        assertEquals(31, clientsRepository.findById(1)?.daysPassed)
        assertEquals(mutableListOf(0, 1), clientsRepository.findById(1)?.interviewResults)
        assertEquals(1, clientsRepository.findById(1)?.trainingPlanId)
    }
}