import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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
    return HttpClient() {
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
                val responseError = response.receive<ResponseError>()
                if (responseError.error.code != 0) {
                    throw ResponseException(response, "Code: ${responseError.error.code}, message: ${responseError.error.message}")
                }
            }
        }
    }
}