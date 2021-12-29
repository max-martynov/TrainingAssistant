package api.vk

import client.Client
import createHttpClient
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDateTime

class VKApiClient {
    private val accessToken =
        "8d8088feeb18744bc2e5a7ed11067faf9cf495fce1c99c6c430e59b7e093f6a45ff827bc0333dd1bd2172" // "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e" for Fake Community
    private val apiVersion = "5.81"
    private val httpClient = createHttpClient()
    private val responseValidator = ResponseValidator()

    suspend fun sendMessageSafely(peerId: Int, text: String, keyboard: String = "", attachment: String = "") {
        try {
            sendMessage(peerId, text, keyboard, attachment)
        } catch (e: ResponseException) {
            println("${LocalDateTime.now()}: Exception while sending a message - ${e.message}\nRetrying...")
            delay(1000L)
            try {
                sendMessage(peerId, text, keyboard, attachment)
            } catch (e: ResponseException) {
                println("${LocalDateTime.now()}: Still exception - ${e.message}\n")
            }
        }
    }

    private suspend fun sendMessage(peerId: Int, text: String, keyboard: String = "", attachment: String = "") {
        val httpResponse = httpClient.post<HttpResponse>(
            "https://api.vk.com/method/messages.send?"
        ) {
            parameter("access_token", accessToken)
            parameter("peer_id", peerId)
            parameter("message", text)
            parameter("keyboard", keyboard)
            parameter("attachment", attachment)
            parameter("v", apiVersion)
        }
        responseValidator.validate(httpResponse)
    }

    suspend fun sendMessageEventAnswerSafely(messageEvent: MessageEvent, eventData: String) {
        try {
            sendMessageEventAnswer(messageEvent, eventData)
        } catch (e: ResponseException) {
            println("${LocalDateTime.now()}: Exception while sending a message event answer - ${e.message}\nRetrying...")
            delay(1000L)
            try {
                sendMessageEventAnswer(messageEvent, eventData)
            } catch (e: ResponseException) {
                println("${LocalDateTime.now()}: Still exception - ${e.message}\n")
            }
        }
    }

    private suspend fun sendMessageEventAnswer(messageEvent: MessageEvent, eventData: String) {
        val httpResponse = httpClient.post<HttpResponse>(
            "https://api.vk.com/method/messages.sendMessageEventAnswer?"
        ) {
            parameter("access_token", accessToken)
            parameter("event_id", messageEvent.eventId)
            parameter("user_id", messageEvent.userId)
            parameter("peer_id", messageEvent.peerId)
            parameter("event_data", eventData)
            parameter("v", apiVersion)
        }
        responseValidator.validate(httpResponse)
    }

    suspend fun convertFileToAttachment(pathToFile: String, client: Client): String {
        val uploadUrl = getMessagesUploadServer(client.id)
        val file = uploadFile(uploadUrl, pathToFile)
        val doc = saveDoc(file)
        val res = "doc${doc.ownerId}_${doc.id}"
        return res
    }

    @Serializable
    data class Response(
        @SerialName("upload_url")
        val uploadUrl: String
    )
    @Serializable
    data class ResponseJson(val response: Response)

    private suspend fun getMessagesUploadServer(peerId: Int): String {
        val responseJson = httpClient.post<ResponseJson>(
            "https://api.vk.com/method/docs.getMessagesUploadServer?"
        ) {
            parameter("access_token", accessToken)
            parameter("type", "doc")
            parameter("peer_id", peerId)
            parameter("v", apiVersion)
        }
        return responseJson.response.uploadUrl
    }

    private suspend fun uploadFile(address: String, pathToFile: String): String {
        val response: ResponseWithFile = httpClient.submitFormWithBinaryData(
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
    private data class ResponseWithFile(val file: String)

    private suspend fun saveDoc(file: String): Doc {
        return httpClient.post<ResponseWithDocs>(
            "https://api.vk.com/method/docs.save?"
        ) {
            parameter("access_token", accessToken)
            parameter("file", file)
            parameter("v", apiVersion)
        }.docs[0]
        //return Pair(response.docs[0].ownerId, response.docs[0].id)
    }

    @Serializable
    private data class ResponseWithDocs(
        @SerialName("response")
        val docs: List<Doc>
    )
}

/*
const val ACCESS_TOKEN =
    "8d8088feeb18744bc2e5a7ed11067faf9cf495fce1c99c6c430e59b7e093f6a45ff827bc0333dd1bd2172" // "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e" for Fake Community

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
}*/