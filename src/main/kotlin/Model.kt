import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

enum class Status {
    ACTIVE, NEW_CLIENT, WAITING_FOR_PAYMENT, WAITING_FOR_RESULTS
}

data class Client(
    val id: Int,
    var status: Status,
    var totalDaysPassed: Int,
    var trainingPlan: TrainingPlan,
    var daysInWeekPassed: Int,
    var interviewResults: MutableList<Int>
)

data class InterviewResults(
    val firstAnswer: Int? = null,
    val secondAnswer: Int? = null
)

/*
class Client(val id: Int) {
    //var daysPassed: Int = 0
    var weeksPassed: Int = 0 // [0, 4]
    //var currentTrainingPlan: TrainingPlan? = null
    val history: MutableList<TrainingPlan> = mutableListOf()

    suspend fun startTrainingPlan(trainingPlan: TrainingPlan) {
        when(weeksPassed) {
            0 -> showRules()
            1, 2, 3 -> sendPlanForNextWeek(trainingPlan)
            4 -> processPayment()
        }
        processWeek(trainingPlan)
    }

    private suspend fun processWeek(trainingPlan: TrainingPlan) = coroutineScope {
        launch {
            repeat(7) {
                /*delay(
                    Duration.between(LocalDateTime.now(), LocalDateTime.now().plusDays(it + 1L).withHour(9))
                        .toMillis()
                )*/
                delay(
                    Duration.between(LocalDateTime.now(), LocalDateTime.now().plusSeconds((1..5).random().toLong()))
                        .toMillis()
                )
                sendMessageAboutTraining(trainingPlan, it)
            }
            /*delay(
                Duration.between(LocalDateTime.now(), LocalDateTime.now().withHour(21))
                    .toMillis()
            )*/
        }
        weeksPassed++
        history.add(trainingPlan)
        startTrainingPlan(determineNextTrainingPlan())
    }

    private fun determineNextTrainingPlan(): TrainingPlan =
        TODO()

    private suspend fun sendMessageAboutTraining(trainingPlan: TrainingPlan, dayNum: Int) {
        val message = "Тренировка на сегодня: " +
                trainingPlan.trainingDays[dayNum].description +
                "\nСуть: " + trainingPlan.trainingDays[dayNum].essence +
                "\nДлительность: " + trainingPlan.trainingDays[dayNum].durationInMinutes
        sendMessage(id, message)
    }

    private suspend fun sendPlanForNextWeek(trainingPlan: TrainingPlan) {
        val message = "Лови план на следующую неделю: " + trainingPlan.link
        sendMessage(id, message)
    }

    private fun processPayment() {
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

*/

interface TrainingPlansRepository {
    fun addTrainingPlan(trainingPlan: TrainingPlan)
    fun findTrainingPlan(id: Int): TrainingPlan?
}

class LocalTrainingPlansRepository : TrainingPlansRepository {

    private val pathToDirectory = "src/main/resources/TrainingPlans/"

    override fun addTrainingPlan(trainingPlan: TrainingPlan) {
        // gg wp
    }

    override fun findTrainingPlan(id: Int): TrainingPlan? {
        val pathToFile = pathToDirectory + id.toString()
        if (!File(pathToFile).exists())
            return null
        val jsonString = File(pathToFile).readText()
        return Json.decodeFromString(jsonString)
    }

}

@Serializable
data class TrainingPlan(
    val id: Int,
    val link: String,
    @SerialName("training_days")
    val trainingDays: List<TrainingDay>
)

@Serializable
data class TrainingDay(
    val description: String,
    val essence: String,
    @SerialName("duration")
    val durationInMinutes: Int
) {
    override fun toString(): String =
        "Описание: $description\nСуть: $essence\nДлительность: $durationInMinutes"
}

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