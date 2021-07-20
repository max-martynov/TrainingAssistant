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

    private suspend fun getMessagesUploadServer(peerId: Int): String {
        @Serializable
        data class Response(
            @SerialName("upload_url")
            val uploadUrl: String
        )

        @Serializable
        data class ResponseJson(val response: Response)
        return httpClient.post<ResponseJson>(
            "https://api.vk.com/method/docs.getMessagesUploadServer?"
        ) {
            parameter(
                "access_token",
                "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e"
            )
            parameter("type", "doc")
            parameter("peer_id", peerId)
            parameter("v", "5.80")
        }.response.uploadUrl
    }

    private suspend fun uploadFile(address: String): String {
        @Serializable
        data class FileResponse(val file: String)

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

    private suspend fun saveDoc(file: String): Pair<Int, Int> {
        @Serializable
        data class Doc(
            val id: Int,
            @SerialName("owner_id")
            val ownerId: Int
        )

        @Serializable
        data class Response(val response: List<Doc>)

        val response = httpClient.post<Response>(
            "https://api.vk.com/method/docs.save?"
        ) {
            parameter(
                "access_token",
                "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e"
            )
            parameter("file", file)
            parameter("v", "5.95")
        }
        return Pair(response.response[0].ownerId, response.response[0].id)
    }
}

fun determineFirstTrainingPlan(client: Client, passedHours: Int? = null): TrainingPlan {
    val month = if (LocalDate.now().dayOfMonth < 30)
        LocalDate.now().monthValue
    else
        calculateNextMonth(LocalDate.now().monthValue)
    val hours = passedHours ?: determineNextHours(client)
    return TrainingPlan(month = month, hours = hours, week = 0)
}

fun determineNextTrainingPlan(client: Client): TrainingPlan? {
    if (client.weeksPassed == 4)
        return null
    val week = (client.trainingPlan.week + 1) % 4
    val month = if (week != 0)
        LocalDate.now().monthValue
    else
        calculateNextMonth(LocalDate.now().monthValue)
    //LocalDate.now().monthValue
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

fun calculateNextMonth(currentMonth: Int): Int =
    maxOf(1, (currentMonth + 1) % 13)
