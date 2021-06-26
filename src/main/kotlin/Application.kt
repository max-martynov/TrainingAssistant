import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.serialization.*
import io.ktor.routing.*

fun main(args: Array<String>) : Unit = io.ktor.server.netty.EngineMain.main(args)

data class ConfirmationJSON(val type: String, val group_id: Long)

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        json()
    }

    routing {
        post("/") {
            val userInfo = call.receive<ConfirmationJSON>()
            call.respond(mapOf("status" to "ok", "body" to "781cdfe0"))
        }
    }
}