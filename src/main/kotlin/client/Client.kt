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
    NEW_CLIENT,
    WAITING_FOR_PLAN,
    WAITING_FOR_START,
    ACTIVE,
    COMPLETING_INTERVIEW0,
    COMPLETING_INTERVIEW1,
    COMPLETING_INTERVIEW2,
    COMPLETING_INTERVIEW3,
    WAITING_FOR_PAYMENT,
}


data class Client(
    val id: Int,
    var status: Status = Status.NEW_CLIENT,
    var previousStatus: Status = status,
    var weeksPassed: Int = 0,
    var daysPassed: Int = 0,
    var trainingPlan: TrainingPlan = TrainingPlan(0, 0, ""),
    var billId: String = ""
) {

    val trial: Boolean
        get() = weeksPassed < 2

    val trialPeriodEnded: Boolean
        get() = weeksPassed == 2

    val hasCompetition: Boolean
        get() = trainingPlan.activityType == 4
}
