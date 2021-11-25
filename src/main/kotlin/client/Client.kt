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
    NEW_CLIENT, ACTIVE, WAITING_FOR_PLAN, WAITING_FOR_START, WAITING_FOR_PAYMENT, WAITING_FOR_RESULTS
}

data class Client(
    val id: Int,
    var trial: Boolean = true,
    var status: Status = Status.NEW_CLIENT,
    var previousStatus: Status = status,
    var daysPassed: Int = -1,
    var weeksPassed: Int = -1, // actually redundant
    var trainingPlan: TrainingPlan = TrainingPlan(-1, -1, -1),
    var interviewResults: MutableList<Int> = mutableListOf(),
    var billId: String = ""
) {
    val interview: Interview
        get() = when (trainingPlan.hours) {
            1 -> InterviewFor1Hour()
            6 -> InterviewFor6Hours()
            else -> InterviewFor10Hours()
        }

    fun completedInterview(): Boolean = interviewResults.size == interview.interviewQuestions.size

    fun isNew(): Boolean = daysPassed == -1
}
