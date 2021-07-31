import io.ktor.client.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object VkAPI {

    private const val accessToken = "8d8088feeb18744bc2e5a7ed11067faf9cf495fce1c99c6c430e59b7e093f6a45ff827bc0333dd1bd2172" // "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e" for Fake Community

    private val httpClient: HttpClient = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        engine {
            threadsCount = 4
        }
    }

    suspend fun sendMessage(peerId: Int, text: String, keyboard: String = "", attachment: String = "") {
        val response = httpClient.post<HttpResponse>(
            "https://api.vk.com/method/messages.send?"
        ) {
            parameter("access_token", accessToken)
            parameter("peer_id", peerId)
            parameter("message", text)
            parameter("keyboard", keyboard)
            parameter("attachment", attachment)
            parameter("v", "5.81")
        }
        //println(response.content.readUTF8Line())
    }

    suspend fun sendMessageEventAnswer(messageEvent: MessageEvent, eventData: String) {
        val response = httpClient.post<HttpResponse>(
            "https://api.vk.com/method/messages.sendMessageEventAnswer?"
        ) {
            parameter("access_token", accessToken)
            parameter("event_id", messageEvent.eventId)
            parameter("user_id", messageEvent.userId)
            parameter("peer_id", messageEvent.peerId)
            parameter("event_data", eventData)
            parameter("v", "5.81")
        }
    }

    @Serializable
    data class Response(
        @SerialName("upload_url")
        val uploadUrl: String
    )
    @Serializable
    data class ResponseJson(val response: Response)

    suspend fun getMessagesUploadServer(peerId: Int): String =
        httpClient.post<ResponseJson>(
            "https://api.vk.com/method/docs.getMessagesUploadServer?"
        ) {
            parameter("access_token", accessToken)
            parameter("type", "doc")
            parameter("peer_id", peerId)
            parameter("v", "5.81")
        }.response.uploadUrl

    @Serializable
    data class FileResponse(val file: String)

    suspend fun uploadFile(address: String, pathToFile: String): String {
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

    suspend fun saveDoc(file: String): Pair<Int, Int> {
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