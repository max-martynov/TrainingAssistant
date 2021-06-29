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

val dataBase = InMemoryDataBase()

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }

    routing()

}