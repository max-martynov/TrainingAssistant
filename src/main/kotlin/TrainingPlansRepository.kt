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
    private val pathToDirectory = "src/main/resources/TrainingPlans"

    private val pathToFile = "$pathToDirectory/$month/$hours/$week.pdf"

    suspend fun prepareAsAttachment(peerId: Int): Pair<Int, Int> {
        val uploadUrl = VkAPI.getMessagesUploadServer(peerId)
        val file = VkAPI.uploadFile(uploadUrl, pathToFile)
        return VkAPI.saveDoc(file)
    }
}

fun determineNextTrainingPlan(client: Client): TrainingPlan? {
    if (client.weeksPassed == 4)
        return null
    val month = if (client.trainingPlan.week != 4)
        client.trainingPlan.month
    else if (client.trainingPlan.month != LocalDate.now().monthValue)
        LocalDate.now().monthValue
    else
        calculateNextMonth(client.trainingPlan.month)
    val week = calculateNextWeek(client.trainingPlan.week)
    return TrainingPlan(month, determineNextHours(client), week)
}

fun determineNextHours(client: Client): Int =
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

fun calculateNextWeek(currentWeek: Int): Int =
    maxOf(1, (currentWeek + 1) % 5)

fun calculateNextMonth(currentMonth: Int): Int =
    maxOf(1, (currentMonth + 1) % 13)
