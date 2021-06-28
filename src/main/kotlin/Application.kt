import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.HttpClient
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.http.VkOkHttpClient
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main(args: Array<String>) : Unit = io.ktor.server.netty.EngineMain.main(args)

@Serializable
data class ConfirmationJSON(val type: String, val group_id: Long)


fun setUp() {
    val vkClientSettings = VkSettings(
        httpClient = VkOkHttpClient(),
        apiVersion = 5.122,
        defaultParams = paramsOf("lang" to "en"),
        maxExecuteRequestsPerSecond = 3, // Default is 3. Provide [VkApi.EXECUTE_MAX_REQUESTS_PER_SECOND_DISABLED] to disable the `execute` queue loop
        backgroundDispatcher = Dispatchers.Default,
        json = Json
    )
    val client = VkApiClient(
        id = 205462754,
        token = "b65e586155b0c081d9c7fc9e7b2ac2add8cf1cf79a1aa5efe9d8e2fe5a1da6b9aa5c563206850f25d8a4e",
        type = VkApiClient.Type.Community,
        settings = vkClientSettings
    )
}

fun Application.module(testing: Boolean = false) {
    //install(ContentNegotiation) {
      //  json()
    //}

    //setUp()

    routing()

}