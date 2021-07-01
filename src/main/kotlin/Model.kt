import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.lang.Thread.sleep
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.schedule
import kotlin.concurrent.timer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

interface DataBase {
    fun addClient(client: Client)
    fun findClient(client: Client): Client?
    fun findClientById(clientId: Int): Client?
    fun containsClient(client: Client): Boolean =
        findClient(client) != null

    fun containsClient(clientId: Int): Boolean =
        findClientById(clientId) != null
}

class InMemoryDataBase : DataBase {
    private val clients = mutableSetOf<Client>()

    override fun addClient(client: Client) {
        clients.add(client)
    }

    override fun findClient(client: Client): Client? =
        clients.find { it -> it == client }

    override fun findClientById(clientId: Int): Client? =
        clients.find { it -> it.id == clientId }
}

val service = Executors.newCachedThreadPool()

class Client(val id: Int) {
    //var daysPassed: Int = 0
    var weeksPassed: Int = 0 // [0, 4]
    var currentTrainingPlan: TrainingPlan? = null
    val history: MutableList<TrainingPlan> = mutableListOf()

    suspend fun startTrainingPlan(trainingPlan: TrainingPlan): Unit = coroutineScope {
        currentTrainingPlan = trainingPlan
        when(weeksPassed) {
            0 -> showRules()
            1, 2, 3 -> sendPlanForNextWeek()
            4 -> requestPayment()
        }
        launch {
            repeat(7) {
                delay(
                    Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds(it * 2L + 1))
                        .toMillis()
                )
                sendMessageAboutTraining(it)
            }
        }
    }

    private suspend fun sendMessageAboutTraining(dayNum: Int) {
        if (currentTrainingPlan == null)
            throw Exception()
        val message = "Тренировка на сегодня: " +
                currentTrainingPlan!!.trainingDays[dayNum].description +
                "\nСуть: " + currentTrainingPlan!!.trainingDays[dayNum].essence +
                "\nДлительность: " + currentTrainingPlan!!.trainingDays[dayNum].durationInMinutes
        sendMessage(id, message)
    }

    private fun sendPlanForNextWeek() {
        // TODO
    }

    private fun requestPayment() {
        // TODO
    }

    private suspend fun showRules() {
        val message = "Отличный выбор!\n" +
                "Со следующего дня вы начнете заниматься по плану, подобранному специально для вас:\n" +
                "https://docs.google.com/spreadsheets/d/1licA3CnVWL--t8-0q2zDZFHhBXL5CPJOW82wwWcbgtU/edit#gid=1205962029\n" +
                "Также каждый день я / мы  / бот (от какого лица мы общаемся я пока не решил) буду напоминать вам, какую тренировку следует сделать, " +
                "а в конце недели вам будет предложено пройти опрос, чтобы сформировать план на следующую неделю."

        sendMessage(id, message)
    }

}

@Serializable
data class TrainingPlan(
    val id: Int,
    @SerialName("training_days")
    val trainingDays: List<TrainingDay>
)

@Serializable
data class TrainingDay(
    val description: String,
    val essence: String,
    @SerialName("duration")
    val durationInMinutes: Int
)

/*
@Serializable
data class IncomingMessage(
    val id: Int,
    val date: Int,
    @SerialName("peer_id")
    val peerId: Int,
    @SerialName("from_id")
    val fromId: Int,
    val text: String,
    @SerialName("random_id")
    val randomId: Int,
    val ref: String? = null,
    @SerialName("ref_source")
    val refSource: String? = null,
    val attachments: List<JsonElement>,
    val important: Boolean,
    val geo: Geo? = null,
    val payload: String? = null,
    val keyboard: Keyboard? = null,
    @SerialName("fwd_messages")
    val fwdMessages: List<MessagePartial>,
    @SerialName("reply_message")
    val replyMessage: JsonElement? = null,
    val action: Action? = null,
    @SerialName("is_hidden")
    val isHidden: Boolean? = null
) {
    @Serializable
    data class Action(
        val type: String,
        @SerialName("member_id")
        val memberId: Int? = null,
        val text: String? = null,
        val email: String? = null,
        val photo: ChatPhoto? = null
    ) {
        @Suppress("unused")
        enum class Type {
            CHAT_PHOTO_UPDATE,
            CHAT_PHOTO_REMOVE,
            CHAT_CREATE,
            CHAT_TITLE_UPDATE,
            CHAT_KICK_USER,
            CHAT_PIN_MESSAGE,
            CHAT_UNPIN_MESSAGE,
            CHAT_INVITE_USER_BY_LINK
        }

        @Serializable
        data class ChatPhoto(
            @SerialName("photo_50")
            val photo50: String? = null,
            @SerialName("photo_100")
            val photo100: String? = null,
            @SerialName("photo_200")
            val photo200: String? = null
        )
    }

    @Serializable
    data class Keyboard(
        @SerialName("one_time")
        val oneTime: Boolean,
        val inline: Boolean,
        val buttons: List<Button>
    ) {
        @Serializable
        data class Button(
            val action: JsonElement? = null,
            val color: String
        ) {
            @Suppress("unused")
            enum class Color {
                PRIMARY,
                SECONDARY,
                NEGATIVE,
                POSITIVE
            }
        }
    }

    @Serializable
    data class Geo(
        val type: String,
        val coordinates: List<Double>? = null,
        val place: Place? = null
    ) {
        @Serializable
        data class Place(
            val id: Int? = null,
            val title: String? = null,
            val latitude: Double? = null,
            val longitude: Double? = null,
            val created: Int? = null,
            val icon: String? = null,
            val country: String? = null,
            val city: String? = null
        )
    }

    fun isFromChat() = peerId > VkApi.CHAT_ID_PREFIX
}

/**
 * Forwarded messages, edited messages, etc
 */
@Serializable
data class MessagePartial(
    val id: Int,
    val date: Int,
    @SerialName("from_id")
    val fromId: Int,
    @SerialName("random_id")
    val randomId: Int? = null,
    val text: String,
    val attachments: List<JsonElement>,
    @SerialName("conversation_message_id")
    val conversationMessageId: Int,
    @SerialName("peer_id")
    val peerId: Int,
    val out: Int? = null,
    @SerialName("update_time")
    val updateTime: Int? = null
)*/