import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

enum class Status {
    ACTIVE, WAITING_FOR_PLAN, WAITING_FOR_START, WAITING_FOR_PAYMENT, WAITING_FOR_RESULTS
}

data class Client(
    val id: Int,
    var status: Status = Status.WAITING_FOR_PLAN,
    var previousStatus: Status = status,
    var daysPassed: Int = -1,
    var trainingPlanId: Int = -1,
    var interviewResults: MutableList<Int> = mutableListOf()
)





/*
enum class Status {
    ACTIVE, NEW_CLIENT, WAITING_FOR_PAYMENT, WAITING_FOR_RESULTS
}

data class Client(
    val id: Int,
    var status: Status,
    var totalDaysPassed: Int,
    var trainingPlan: TrainingPlan,
    var daysInWeekPassed: Int,
    var interviewResults: MutableList<Int>
)

data class InterviewResults(
    val firstAnswer: Int? = null,
    val secondAnswer: Int? = null
)

interface TrainingPlansRepository {
    fun addTrainingPlan(trainingPlan: TrainingPlan)
    fun findTrainingPlan(id: Int): TrainingPlan?
}

class LocalTrainingPlansRepository : TrainingPlansRepository {

    var size = 0

    private val pathToDirectory = "src/main/resources/TrainingPlans/"

    override fun addTrainingPlan(trainingPlan: TrainingPlan) {
        // gg wp
    }

    override fun findTrainingPlan(id: Int): TrainingPlan? {
        val pathToFile = "$pathToDirectory$id.json"
        if (!File(pathToFile).exists())
            return null
        val jsonString = File(pathToFile).readText()
        return Json.decodeFromString(jsonString)
    }

}

@Serializable
data class TrainingPlan(
    val id: Int,
    val link: String,
    @SerialName("training_days")
    val trainingDays: List<TrainingDay>
)

@Serializable
data class TrainingDay(
    val description: String,
    val essence: String,
    @SerialName("duration")
    val durationInMinutes: Int
) {
    override fun toString(): String =
        "Описание: $description\nСуть: $essence\nДлительность: ${durationInMinutes / 60}:${durationInMinutes % 60}"

}*/