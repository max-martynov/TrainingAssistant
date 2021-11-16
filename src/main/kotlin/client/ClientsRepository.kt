import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.naming.Context
import javax.naming.InitialContext
import kotlin.concurrent.withLock


interface ClientsRepository {
    suspend fun add(client: Client)
    suspend fun findById(clientId: Int): Client?
    suspend fun getAll(): List<Client>
    suspend fun update(
        id: Int,
        newTrial: Boolean? = null,
        newStatus: Status? = null,
        newDaysPassed: Int? = null,
        newWeeksPassed: Int? = null,
        newTrainingPlan: TrainingPlan? = null,
        newInterviewResults: MutableList<Int>? = null,
        newBillId: String? = null
    )
    suspend fun delete(clientId: Int)

    fun clear()
}

class InMemoryClientsRepository : ClientsRepository {
    private val clients = mutableListOf<Client>()//Collections.synchronizedList(mutableListOf<Client>())

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
        newTrial: Boolean?,
        newStatus: Status?,
        newDaysPassed: Int?,
        newWeeksPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newInterviewResults: MutableList<Int>?,
        newBillId: String?
    ) {
        val client = findById(id) ?: return
        lock.withLock {
            clients.remove(client)
            if (newTrial != null)
                client.trial = newTrial
            if (newStatus != null) {
                client.previousStatus = client.status
                client.status = newStatus
            }
            if (newDaysPassed != null)
                client.daysPassed = newDaysPassed
            if (newWeeksPassed != null)
                client.weeksPassed = newWeeksPassed
            if (newTrainingPlan != null)
                client.trainingPlan = newTrainingPlan
            if (newInterviewResults != null)
                client.interviewResults = newInterviewResults
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
                    it[trial] = client.trial
                    it[status] = client.status.toString()
                    it[previousStatus] = client.previousStatus.toString()
                    it[daysPassed] = client.daysPassed
                    it[weeksPassed] = client.weeksPassed
                    it[trainingPlanMonth] = client.trainingPlan.month
                    it[trainingPlanWeek] = client.trainingPlan.week
                    it[trainingPlanHours] = client.trainingPlan.hours
                    it[interviewResults] = client.interviewResults.joinToString(separator = "")
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
            trial = client[Clients.trial],
            status = Status.valueOf(client[Clients.status]),
            previousStatus = Status.valueOf(client[Clients.previousStatus]),
            daysPassed = client[Clients.daysPassed],
            weeksPassed = client[Clients.weeksPassed],
            trainingPlan = TrainingPlan(
                month = client[Clients.trainingPlanMonth],
                hours = client[Clients.trainingPlanHours],
                week = client[Clients.trainingPlanWeek],
            ),
            interviewResults = client[Clients.interviewResults].map { it.toString().toInt() }.toMutableList(),
            billId = client[Clients.billId]
        )
    }

    override suspend fun getAll(): List<Client> = newSuspendedTransaction {
        Clients.selectAll().map { convertClientToDataClass(it) }
    }

    override suspend fun update(
        id: Int,
        newTrial: Boolean?,
        newStatus: Status?,
        newDaysPassed: Int?,
        newWeeksPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newInterviewResults: MutableList<Int>?,
        newBillId: String?
    ) {
        newSuspendedTransaction {
            Clients.update({ Clients.id eq id }) {
                if (newTrial != null)
                    it[trial] = newTrial
                if (newStatus != null) {
                    it[previousStatus] = Clients.status
                    it[status] = newStatus.toString()
                }
                if (newDaysPassed != null) it[daysPassed] = newDaysPassed
                if (newWeeksPassed != null) it[weeksPassed] = newWeeksPassed
                if (newTrainingPlan != null) {
                    it[trainingPlanMonth] = newTrainingPlan.month
                    it[trainingPlanHours] = newTrainingPlan.hours
                    it[trainingPlanWeek] = newTrainingPlan.week
                }
                if (newInterviewResults != null) it[interviewResults] = newInterviewResults.joinToString(separator = "")
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
    val trial = bool("trial")
    val status = varchar("status", 20)
    val previousStatus = varchar("previous_status", 20)
    val daysPassed = integer("days_passed")
    val weeksPassed = integer("weeks_passed")
    val trainingPlanMonth = integer("training_plan_month")
    val trainingPlanHours = integer("training_plan_hours")
    val trainingPlanWeek = integer("training_plan_week")
    val interviewResults = varchar("interview_results", 10)
    val billId = varchar("bill_id", 20)
    override val primaryKey = PrimaryKey(id)
}


