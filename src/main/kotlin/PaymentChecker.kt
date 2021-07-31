import VkAPI.sendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.management.ManagementFactory

@Serializable
private data class Event(
    val type: String,
    @SerialName("object")
    val messageEvent: MessageEvent,
    @SerialName("group_id")
    val groupId: Long
)

@Serializable
data class MessageEvent(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("peer_id")
    val peerId: Int,
    @SerialName("event_id")
    val eventId: String
)

suspend fun checkPayment(notification: String) = coroutineScope {
    val messageEvent = Json { ignoreUnknownKeys = true }.decodeFromString<Event>(notification).messageEvent
    val client = clientsRepository.findById(messageEvent.userId) ?: return@coroutineScope
    if (client.status != Status.WAITING_FOR_PAYMENT) {
        VkAPI.sendMessageEventAnswer(messageEvent, getShowSnackbarString("Оплата прошла успешно. Хороших тренировок!"))
    }
    else if (QiwiAPI.isPaid(client.billId)) {
        async { confirmPayment(client, messageEvent) }
        async { sendMessage(client.id, "Чтобы получить недельный план и начать тренировочный процесс, нажмите \"Начать цикл\".") }
    }
    else {
        VkAPI.sendMessageEventAnswer(
            messageEvent,
            getShowSnackbarString("К сожалению, данные об оплате еще не поступили! Попробуйте позже.")
        )
    }
    println("Current number of threads = ${ManagementFactory.getThreadMXBean().threadCount}")
}

suspend fun confirmPayment(client: Client, messageEvent: MessageEvent?) {
    val phrase =
        if (client.trial)
            "Оплата подтверждена! Спасибо, что решили продолжить тренировки по подписке."
        else
            "Оплата подтверждена! Надеюсь, Вам понравятся тренировки в этом месяце."
    updateClient(client)
    if (messageEvent != null)
        VkAPI.sendMessageEventAnswer(messageEvent, getShowSnackbarString(phrase))
}

suspend fun updateClient(client: Client) {
    if (client.trial) { // for clients after trial
        clientsRepository.update(
            client.id,
            newTrial = false,
            newStatus = Status.WAITING_FOR_START,
            newWeeksPassed = 0,
            newDaysPassed = 0
        )
    } else if (client.previousStatus == Status.WAITING_FOR_RESULTS && client.completedInterview()) { // for those who completed month too fast
        clientsRepository.update(
            client.id,
            newWeeksPassed = 0,
            newDaysPassed = 0
        )
        val updatedClient = clientsRepository.findById(client.id)!!
        clientsRepository.update(
            client.id,
            newStatus = Status.WAITING_FOR_START,
            newTrainingPlan = determineNextTrainingPlan(updatedClient),
            newInterviewResults = mutableListOf()
        )
    } else { // for usual clients
        clientsRepository.update(
            client.id,
            newStatus = client.previousStatus,
            newDaysPassed = 0,
            newWeeksPassed = 0
        )
    }
}

fun getShowSnackbarString(text: String): String =
    """
        {
            "type": "show_snackbar", 
            "text": "$text"
        }
    """.trimIndent()
