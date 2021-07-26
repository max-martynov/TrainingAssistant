import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


interface ClientsRepository {
    fun add(client: Client)
    fun findById(clientId: Int): Client?
    fun getAll(): List<Client>
    fun update(
        id: Int,
        newTrial: Boolean? = null,
        newStatus: Status? = null,
        newDaysPassed: Int? = null,
        newWeeksPassed: Int? = null,
        newTrainingPlan: TrainingPlan? = null,
        newInterviewResults: MutableList<Int>? = null
    )

    fun clear()
}

class InMemoryClientsRepository : ClientsRepository {
    private val clients = mutableListOf<Client>()//Collections.synchronizedList(mutableListOf<Client>())

    private val lock = ReentrantLock()

    override fun add(client: Client) {
        lock.withLock{
            if (clients.count { it.id == client.id } == 0)
                clients.add(client)
        }
    }

    override fun findById(clientId: Int): Client? {
        lock.withLock {
            return clients.find { it.id == clientId }
        }
    }

    override fun getAll(): List<Client> {
        lock.withLock {
            return clients.toList()
        }
    }


    override fun update(
        id: Int,
        newTrial: Boolean?,
        newStatus: Status?,
        newDaysPassed: Int?,
        newWeeksPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newInterviewResults: MutableList<Int>?
    ) {
        lock.withLock {
            val client = findById(id) ?: return
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
            clients.add(client)
        }
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

    override fun add(client: Client) {
        transaction {
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
                }
            }
        }
    }

    override fun findById(clientId: Int): Client? {
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
            interviewResults = client[Clients.interviewResults].map { it.toString().toInt() }.toMutableList()
        )
    }

    override fun getAll(): List<Client> = transaction {
        Clients.selectAll().map { convertClientToDataClass(it) }
    }

    override fun update(
        id: Int,
        newTrial: Boolean?,
        newStatus: Status?,
        newDaysPassed: Int?,
        newWeeksPassed: Int?,
        newTrainingPlan: TrainingPlan?,
        newInterviewResults: MutableList<Int>?
    ) {
        transaction {
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
            }
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
    override val primaryKey = PrimaryKey(id)
}


