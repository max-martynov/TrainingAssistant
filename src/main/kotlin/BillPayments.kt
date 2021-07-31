import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime

object QiwiAPI {
    private val secretKey =
        "eyJ2ZXJzaW9uIjoiUDJQIiwiZGF0YSI6eyJwYXlpbl9tZXJjaGFudF9zaXRlX3VpZCI6IjRzN2c0My0wMCIsInVzZXJfaWQiOiI3OTUzNTQ4NjMzMCIsInNlY3JldCI6ImNmYWU3YWM1MDQ1ODdlNGE3NjhkOTIzYzZiMGY0NTM0MmIwNTk4MTQyMGQ2YWQzYjg5OWU2NDFjMzRmYzgwYTcifX0="

    private val httpClient: HttpClient = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun getPayUrl(billId: String): String {
        val response = httpClient.put<Response>("https://api.qiwi.com/partner/bill/v1/bills/$billId") {
            headers {
                append("Authorization", "Bearer $secretKey")
                append("Content-Type", "application/json")
                append("Accept", "application/json")
            }
            body = Bill(
                Amount(paymentAmount, "RUB"),
                OffsetDateTime.now().plusYears(3).toString(),
                CustomFields("Maksym-MgT6FuGqP7")
            )
        }
        return response.payUrl
    }

    suspend fun isPaid(billId: String): Boolean {
        val response = httpClient.get<Response>("https://api.qiwi.com/partner/bill/v1/bills/$billId") {
            headers {
                append("Authorization", "Bearer $secretKey")
                append("Accept", "application/json")
            }
        }
        return response.status.value == "PAID"
    }

    @Serializable
    data class CustomFields(val themeCode: String)

    @Serializable
    data class Amount(val value: Int, val currency: String)

    @Serializable
    data class Bill(
        val amount: Amount,
        val expirationDateTime: String,
        val customFields: CustomFields
    )

    @Serializable
    data class Status(val value: String, val changedDateTime: String)

    @Serializable
    data class Response(val status: Status, val payUrl: String)
}