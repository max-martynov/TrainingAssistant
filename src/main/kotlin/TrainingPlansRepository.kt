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
        val uploadUrl = getMessagesUploadServer(peerId)
        val file = uploadFile(uploadUrl)
        return saveDoc(file)
    }

    private val httpClient: HttpClient = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    @Serializable
    data class Response(
        @SerialName("upload_url")
        val uploadUrl: String
    )

    @Serializable
    data class ResponseJson(val response: Response)

    private suspend fun getMessagesUploadServer(peerId: Int): String {

        return httpClient.post<ResponseJson>(
            "https://api.vk.com/method/docs.getMessagesUploadServer?"
        ) {
            parameter("access_token", accessToken)
            parameter("type", "doc")
            parameter("peer_id", peerId)
            parameter("v", "5.81")
        }.response.uploadUrl
    }

    @Serializable
    data class FileResponse(val file: String)

    private suspend fun uploadFile(address: String): String {


        val response: FileResponse = httpClient.submitFormWithBinaryData(
            url = address,
            formData = formData {
                append("file", File(pathToFile).readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "application/pdf")
                    append(HttpHeaders.ContentDisposition, "filename=TrainingPlan.pdf")
                })
            }
        )
        return response.file
    }

    @Serializable
    data class Doc(
        val id: Int,
        @SerialName("owner_id")
        val ownerId: Int
    )

    @Serializable
    data class Responses(val response: List<Doc>)

    private suspend fun saveDoc(file: String): Pair<Int, Int> {
        val response = httpClient.post<Responses>(
            "https://api.vk.com/method/docs.save?"
        ) {
            parameter("access_token", accessToken)
            parameter("file", file)
            parameter("v", "5.81")
        }
        return Pair(response.response[0].ownerId, response.response[0].id)
    }
}

fun determineNextTrainingPlan(client: Client): TrainingPlan? {
    if (client.weeksPassed == 4)
        return null
    val month = if (client.trainingPlan.week != 4)
        client.trainingPlan.month
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
