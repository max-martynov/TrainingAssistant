import com.petersamokhin.vksdk.core.client.VkApiClient
import com.petersamokhin.vksdk.core.http.paramsOf
import com.petersamokhin.vksdk.core.model.VkSettings
import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import com.petersamokhin.vksdk.core.model.event.MessageNew
import com.petersamokhin.vksdk.http.VkOkHttpClient
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalTime


fun getType(call: String): String =
    call.substring(9, call.indexOf('"', 9))




fun Application.routing() {
    routing {
        post("/") {
            withContext(Dispatchers.IO) {
                call.receive<InputStream>().use {
                    val notification = it.readBytes().decodeToString()
                    when (getType(notification)) {
                        "message_new" -> {
                            call.respondText("ok")
                            handleIncomingMessage(notification)
                        }
                        /*"vkpay_transaction" -> {
                            call.respondText("ok")
                            receivePayment(notification)
                        }*/
                        "message_event" -> {
                            call.respondText("ok")
                            checkPayment(notification)
                        }
                        "confirmation" -> {
                            val responseString = "be0eac70" // Warning! May change after some time
                            call.respondText(responseString)
                        }
                        else -> call.respondText("ok")
                    }
                }
            }
        }
    }
}
