import eventHandlers.IncomingMessageHandler
import eventHandlers.MessageEventHandler
import api.vk.EventWithIncomingMessage
import api.vk.EventWithMessageEvent
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream


fun getType(call: String): String =
    call.substring(9, call.indexOf('"', 9))

fun Application.routing(
    incomingMessageHandler: IncomingMessageHandler,
    messageEventHandler: MessageEventHandler
) {
    routing {
        post("/") {
            withContext(Dispatchers.IO) {
                call.receive<InputStream>().use {
                    val notification = it.readBytes().decodeToString()
                    if (notification.length < 9)
                        return@withContext
                    when (getType(notification)) {
                        "message_new" -> {
                            call.respondText("ok")
                            val event: EventWithIncomingMessage = Json { ignoreUnknownKeys = true }.decodeFromString(notification)
                            incomingMessageHandler.receiveMessage(event.message)
                        }
                        "message_event" -> {
                            call.respondText("ok")
                            val event: EventWithMessageEvent = Json { ignoreUnknownKeys = true }.decodeFromString(notification)
                            messageEventHandler.checkPayment(event.messageEvent)
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
