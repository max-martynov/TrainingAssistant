import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

class TrainingPlansRepository(private val pathToDirectory: String) {

    constructor(pathToDirectory: String, numberOfTrainingPlans: Int) : this(pathToDirectory) {
        repeat(numberOfTrainingPlans) {
            add(it)
        }
    }

    private val trainingPlanIds = mutableSetOf<Int>()

    fun contains(trainingPlanId: Int): Boolean =
        trainingPlanIds.contains(trainingPlanId)
    fun add(trainingPlanId: Int) {
        trainingPlanIds.add(trainingPlanId)
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

    /**
     * Returns Pair(owner_id, id) or null if such plan doesn't exist
     */
    suspend fun prepareAsAttachment(trainingPlanId: Int, peerId: Int): Pair<Int, Int> {
        val uploadUrl = getMessagesUploadServer(peerId)
        val file = uploadFile(uploadUrl, "$pathToDirectory/$trainingPlanId.pdf")
        return saveDoc(file)
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
    private suspend fun uploadFile(address: String, pathToFile: String): String {
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
            parameter("v", "5.80")
        }
        return Pair(response.response[0].ownerId,response.response[0].id)
    }

}