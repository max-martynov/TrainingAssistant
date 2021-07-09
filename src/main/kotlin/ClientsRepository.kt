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
    )
}

class InMemoryClientsRepository : ClientsRepository {
    private val clients = Collections.synchronizedSet(mutableSetOf<Client>())

    override suspend fun add(client: Client) {
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
        newTrainingPlanId: Int?
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
        clients.add(client)
    }
}

/*
interface ClientRepository {
    suspend fun add(client: Client)
    suspend fun findClientById(clientId: Int): Client?
    suspend fun getAllClients(): List<Client>
    suspend fun updateClient(
        id: Int,
        newStatus: Status? = null,
        newTotalDaysPassed: Int? = null,
        newTrainingPlan: TrainingPlan? = null,
        newDaysInWeekPassed: Int? = null,
        newInterviewResults: MutableList<Int>? = null
    )
}

class InMemoryClientRepository : ClientRepository {
    private val clients = Collections.synchronizedSet(mutableSetOf<Client>())

    override suspend fun add(client: Client) {
        clients.add(client)
    }

    override suspend fun findClientById(clientId: Int): Client? =
        clients.find { it -> it.id == clientId }

    override suspend fun getAllClients(): List<Client> =
        clients.toList()

    override suspend fun updateClient(
        id: Int,
        newStatus: Status?,
        newTotalDaysPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newDaysInWeekPassed: Int?,
        newInterviewResults: MutableList<Int>?
    ) {
        val client = findClientById(id) ?: return
        clients.remove(client)
        if (newStatus != null) client.status = newStatus
        if (newTotalDaysPassed != null) client.totalDaysPassed = newTotalDaysPassed
        if (newTrainingPlan != null) client.trainingPlan = newTrainingPlan
        if (newDaysInWeekPassed != null) client.daysInWeekPassed = newDaysInWeekPassed
        if (newInterviewResults != null) client.interviewResults = newInterviewResults
        clients.add(client)
    }
}

class InDataBaseClientRepository(connStr: String, driver: String) : ClientRepository {

    init {
        Database.connect(connStr, driver)

        transaction {
            SchemaUtils.create(Clients)
        }
    }

    override suspend fun add(client: Client) {
        Clients.insert {
            it[id] = client.id
            it[status] = client.status.toString()
            it[totalDaysPassed] = client.totalDaysPassed
            it[trainingPlanId] = client.trainingPlan.id
            it[daysInWeekPassed] = client.daysInWeekPassed
            it[interviewResults] = client.interviewResults.joinToString(separator = "")
        }
    }

    override suspend fun findClientById(clientId: Int): Client? {
        val query = Clients.select { Clients.id eq clientId }
        if (query.empty())
            return null
        return try {
            convertClientToDataClass(query.toList()[0])
        } catch (e: Exception) {
            null
        }
    }

    private fun convertClientToDataClass(client: ResultRow): Client {
        val trainingPlan = trainingPlansRepository.findTrainingPlan(client[Clients.trainingPlanId]) ?: throw Exception()
        return Client(
            id = client[Clients.id],
            status = Status.valueOf(client[Clients.status]),
            totalDaysPassed = client[Clients.totalDaysPassed],
            trainingPlan = trainingPlan,
            daysInWeekPassed = client[Clients.daysInWeekPassed],
            interviewResults = client[Clients.interviewResults].map { it.toString().toInt() }.toMutableList()
        )
    }

    override suspend fun getAllClients(): List<Client> =
        Clients.selectAll().map { convertClientToDataClass(it) }

    override suspend fun updateClient(
        id: Int,
        newStatus: Status?,
        newTotalDaysPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newDaysInWeekPassed: Int?,
        newInterviewResults: MutableList<Int>?
    ) {
        Clients.update({ Clients.id eq id }) {
            if (newStatus != null) it[status] = newStatus.toString()
            if (newTotalDaysPassed != null) it[totalDaysPassed] = newTotalDaysPassed
            if (newTrainingPlan != null) it[trainingPlanId] = newTrainingPlan.id
            if (newDaysInWeekPassed != null) it[daysInWeekPassed] = newDaysInWeekPassed
            if (newInterviewResults != null) it[interviewResults] = newInterviewResults.joinToString(separator = "")
        }
    }
}

object Clients : Table() {
    val id = integer("id")
    val status = varchar("status", 10)
    val totalDaysPassed = integer("total_days_passed")
    val trainingPlanId = integer("training_plan_id")
    val daysInWeekPassed = integer("days_in_week_passed")
    val interviewResults = varchar("interview_results", 10)

    override val primaryKey = PrimaryKey(id)
}

*/


