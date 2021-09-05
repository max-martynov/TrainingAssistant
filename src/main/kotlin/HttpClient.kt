import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import java.nio.ByteBuffer

@Serializable
private data class ResponseError(val error: Error = Error(0, "")) {
    @Serializable
    data class Error(
        @SerialName("error_code")
        val code: Int,
        @SerialName("error_msg")
        val message: String
    )
}

fun createHttpClient(): HttpClient {
    return HttpClient(Apache) {
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
        HttpResponseValidator {
            validateResponse { response ->
                val buffer = ByteArray(10000)
                response.content.readAvailable(buffer)
                val body = buffer.decodeToString()
                println(body)
            }
        }
    }
}