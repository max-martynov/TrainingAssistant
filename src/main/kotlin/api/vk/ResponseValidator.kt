package api.vk

import io.ktor.client.call.*
import io.ktor.client.features.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.statement.HttpResponse

@Serializable
data class ResponseError(val error: Error = Error(0, "")) {
    @Serializable
    data class Error(
        @SerialName("error_code")
        val code: Int,
        @SerialName("error_msg")
        val message: String
    )
}

class ResponseValidator {
    suspend fun validate(httpResponse: HttpResponse) {
        val error = httpResponse.receive<ResponseError>().error
        if (error.code != 0) {
            throw ResponseException(httpResponse, "Code: ${error.code}, message: ${error.message}")
        }
    }
}