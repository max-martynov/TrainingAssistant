import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDate

data class TrainingPlan(
    val month: Int,
    val hours: Int,
    val week: Int,
) {
    private var numberOfReviews: Int = 0
    private var sum: Int = 0
    val rating: Double
        get() = if (numberOfReviews == 0) 0.0 else sum * 1.0 / numberOfReviews
    fun processReview(newReview: Int) {
        numberOfReviews++
        sum += newReview
    }
}

class TrainingPlansRepository(
    private val pathToDirectory: String
) {

    fun getPathToFile(trainingPlan: TrainingPlan) =
        "$pathToDirectory/${trainingPlan.month}/${trainingPlan.hours}/${trainingPlan.week}.pdf"

    fun determineNextTrainingPlan(client: Client): TrainingPlan {
        val (month, _, week) = client.trainingPlan
        val nextHours = determineNextHours(client)
        if (week < 4)
            return TrainingPlan(month, nextHours, week + 1)
        else if (month != LocalDate.now().monthValue)
            return TrainingPlan(LocalDate.now().monthValue, nextHours, 1)
        else
            return TrainingPlan(month, nextHours, 4 + ((week - 3) % 3))
    }

    private fun determineNextHours(client: Client): Int =
        when (client.trainingPlan.hours) {
            1 -> {
                if (client.interviewResults[1] == 1)
                    1
                else if (client.interviewResults[2] == 0)
                    6
                else
                    10
            }
            6 -> {
                if (client.interviewResults[3] == 0)
                    1
                else if (client.interviewResults[1] == 0)
                    10
                else
                    6
            }
            10 -> {
                if (client.interviewResults[3] == 0)
                    1
                else if (client.interviewResults[1] == 0)
                    6
                else
                    10
            }
            else -> -1
        }

    private fun calculateNextWeek(currentWeek: Int): Int =
        maxOf(1, (currentWeek + 1) % 5)

    private fun calculateNextMonth(currentMonth: Int): Int =
        maxOf(1, (currentMonth + 1) % 13)
}
