import client.Client
import client.Status
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


interface ClientsRepository {
    suspend fun add(client: Client)
    suspend fun findById(clientId: Int): Client?
    suspend fun getAll(): List<Client>
    suspend fun update(
        id: Int,
        newStatus: Status? = null,
        newWeeksPassed: Int? = null,
        newDaysPassed: Int? = null,
        newTrainingPlan: TrainingPlan? = null,
        newBillId: String? = null
    )
    suspend fun delete(clientId: Int)

    fun clear()
}

class InMemoryClientsRepository : ClientsRepository {
    private val clients = mutableListOf<Client>()//Collections.synchronizedList(mutableListOf<client.Client>())

    private val lock = ReentrantLock()

    override suspend fun add(client: Client) {
        lock.withLock{
            if (clients.count { it.id == client.id } == 0)
                clients.add(client)
        }
    }

    override suspend fun findById(clientId: Int): Client? {
        lock.withLock {
            return clients.find { it.id == clientId }
        }
    }

    override suspend fun getAll(): List<Client> {
        lock.withLock {
            return clients.toList()
        }
    }

    override suspend fun update(
        id: Int,
        newStatus: Status?,
        newWeeksPassed: Int?,
        newDaysPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newBillId: String?
    ) {
        val client = findById(id) ?: return
        lock.withLock {
            clients.remove(client)
            if (newStatus != null) {
                client.previousStatus = client.status
                client.status = newStatus
            }
            if (newWeeksPassed != null)
                client.weeksPassed = newWeeksPassed
            if (newDaysPassed != null)
                client.daysPassed = newDaysPassed
            if (newTrainingPlan != null)
                client.trainingPlan = newTrainingPlan
            if (newBillId != null)
                client.billId = newBillId
            clients.add(client)
        }
    }

    override suspend fun delete(clientId: Int) {
        lock.withLock {
            clients.remove(clients.find { it.id == clientId })
        }
    }

    override fun clear() {
        clients.clear()
    }
}

class InDataBaseClientsRepository() : ClientsRepository {

    init {
        Database.connect(url = "jdbc:h2:~/mem_test", driver = "org.h2.Driver", user = "org.h2.Driver", password = "aRootPassword")

        transaction {
            SchemaUtils.create(Clients)
            addLogger(StdOutSqlLogger)
        }
    }

    override suspend fun add(client: Client) {
        newSuspendedTransaction {
            if (findById(client.id) == null) {
                Clients.insert {
                    it[id] = client.id
                    it[status] = client.status.toString()
                    it[previousStatus] = client.previousStatus.toString()
                    it[daysPassed] = client.daysPassed
                    it[weeksPassed] = client.weeksPassed
                    it[activityType] = client.trainingPlan.activityType
                    it[duration] = client.trainingPlan.duration
                    it[plan] = client.trainingPlan.plan
                    it[billId] = client.billId
                }
            }
        }
    }

    override suspend fun findById(clientId: Int): Client? {
        return newSuspendedTransaction {
            val query = Clients.select { Clients.id eq clientId }
            if (query.empty())
                return@newSuspendedTransaction null
            return@newSuspendedTransaction try {
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
            weeksPassed = client[Clients.weeksPassed],
            daysPassed = client[Clients.daysPassed],
            trainingPlan = TrainingPlan(client[Clients.activityType], client[Clients.duration], client[Clients.plan]),
            billId = client[Clients.billId]
        )
    }

    override suspend fun getAll(): List<Client> = newSuspendedTransaction {
        Clients.selectAll().map { convertClientToDataClass(it) }
    }

    override suspend fun update(
        id: Int,
        newStatus: Status?,
        newWeeksPassed: Int?,
        newDaysPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newBillId: String?
    ) {
        newSuspendedTransaction {
            Clients.update({ Clients.id eq id }) {
                if (newStatus != null) {
                    it[previousStatus] = Clients.status
                    it[status] = newStatus.toString()
                }
                if (newWeeksPassed != null)
                    it[weeksPassed] = newWeeksPassed
                if (newDaysPassed != null)
                    it[daysPassed] = newDaysPassed
                if (newTrainingPlan != null) {
                    it[activityType] = newTrainingPlan.activityType
                    it[duration] = newTrainingPlan.duration
                    it[plan] = newTrainingPlan.plan
                }
                if (newBillId != null) it[billId] = newBillId
            }
        }
    }

    override suspend fun delete(clientId: Int) {
        newSuspendedTransaction {
            Clients.deleteWhere { Clients.id eq clientId }
        }
    }

    override fun clear() {
        transaction {
            SchemaUtils.drop(Clients)
            SchemaUtils.create(Clients)
        }
    }
}

object Clients : Table() {
    val id = integer("id")
    val status = varchar("status", 30)
    val previousStatus = varchar("previous_status", 30)
    val weeksPassed = integer("weeks_passed")
    val daysPassed = integer("days_passed")
    val activityType = integer("activity_type")
    val duration = integer("duration")
    val plan = varchar("training_plan", 2000)
    val billId = varchar("bill_id", 20)
    override val primaryKey = PrimaryKey(id)
}


