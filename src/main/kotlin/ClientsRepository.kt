import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


interface ClientsRepository {
    suspend fun add(client: Client)
    suspend fun findById(clientId: Int): Client?
    suspend fun getAll(): List<Client>
    suspend fun update(
        id: Int,
        newStatus: Status? = null,
        newDaysPassed: Int? = null,
        newTrainingPlanId: Int? = null,
        newInterviewResults: MutableList<Int>? = null
    )
    fun clear()
}

class InMemoryClientsRepository : ClientsRepository {
    private val clients = Collections.synchronizedList(mutableListOf<Client>())

    override suspend fun add(client: Client) {
        if (clients.count { it.id == client.id } == 0)
            clients.add(client)
    }

    override suspend fun findById(clientId: Int): Client? =
        clients.find { it -> it.id == clientId }

    override suspend fun getAll(): List<Client> =
        clients.toList()

    override suspend fun update(
        id: Int,
        newStatus: Status?,
        newDaysPassed: Int?,
        newTrainingPlanId: Int?,
        newInterviewResults: MutableList<Int>?
    ) {
        val client = findById(id) ?: return
        clients.remove(client)
        if (newStatus != null) {
            client.previousStatus = client.status
            client.status = newStatus
        }
        if (newDaysPassed != null)
            client.daysPassed = newDaysPassed
        if (newTrainingPlanId != null)
            client.trainingPlanId = newTrainingPlanId
        if (newInterviewResults != null)
            client.interviewResults = newInterviewResults
        clients.add(client)
    }

    override fun clear() {
        clients.clear()
    }
}

class InDataBaseClientsRepository(
    connStr: String = "jdbc:h2:~/test",
    driver: String = "org.h2.Driver"
) : ClientsRepository {

    init {
        Database.connect(connStr, driver)

        transaction {
            SchemaUtils.create(Clients)
            addLogger(StdOutSqlLogger)
        }
    }

    override suspend fun add(client: Client) {
        coroutineScope {
            transaction {
                runBlocking {
                    if (findById(client.id) == null) {
                        Clients.insert {
                            it[id] = client.id
                            it[status] = client.status.toString()
                            it[previousStatus] = client.previousStatus.toString()
                            it[daysPassed] = client.daysPassed
                            it[trainingPlanId] = client.trainingPlanId
                            it[interviewResults] = client.interviewResults.joinToString(separator = "")
                        }
                    }
                }
            }
        }
    }

    override suspend fun findById(clientId: Int): Client? {
        return transaction {
            val query = Clients.select { Clients.id eq clientId }
            if (query.empty())
                return@transaction null
            return@transaction try {
                convertClientToDataClass(query.toList()[0])
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun convertClientToDataClass(client: ResultRow): Client {
        return Client(
            id = client[Clients.id],
            status = Status.valueOf(client[Clients.status]),
            previousStatus = Status.valueOf(client[Clients.previousStatus]),
            daysPassed = client[Clients.daysPassed],
            trainingPlanId = client[Clients.trainingPlanId],
            interviewResults = client[Clients.interviewResults].map { it.toString().toInt() }.toMutableList()
        )
    }

    override suspend fun getAll(): List<Client> = transaction {
        Clients.selectAll().map { convertClientToDataClass(it) }
    }

    override suspend fun update(
        id: Int,
        newStatus: Status?,
        newDaysPassed: Int?,
        newTrainingPlanId: Int?,
        newInterviewResults: MutableList<Int>?
    ) {
        transaction {
            Clients.update({ Clients.id eq id }) {
                if (newStatus != null) {
                    it[previousStatus] = Clients.status
                    it[status] = newStatus.toString()
                }
                if (newDaysPassed != null) it[daysPassed] = newDaysPassed
                if (newTrainingPlanId != null) it[trainingPlanId] = newTrainingPlanId
                if (newInterviewResults != null) it[interviewResults] = newInterviewResults.joinToString(separator = "")
            }
        }
    }

    override fun clear() {
        transaction {
            SchemaUtils.drop(Clients)
        }
    }
}

object Clients : Table() {
    val id = integer("id")
    val status = varchar("status", 20)
    val previousStatus = varchar("previous_status", 20)
    val daysPassed = integer("days_passed")
    val trainingPlanId = integer("training_plan_id")
    val interviewResults = varchar("interview_results", 10)

    override val primaryKey = PrimaryKey(id)
}


