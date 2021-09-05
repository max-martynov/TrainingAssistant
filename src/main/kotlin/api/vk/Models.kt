package api.vk

import com.petersamokhin.vksdk.core.api.VkApi
import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import com.petersamokhin.vksdk.core.model.event.MessagePartial
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


/**
 * Events to receive.
 */

@Serializable
sealed class Event {
    abstract val type: String
    @SerialName("group_id")
    abstract val groupId: Long
}

@Serializable
data class EventWithIncomingMessage(
    override val type: String,
    @SerialName("object")
    val message: IncomingMessage,
    @SerialName("group_id")
    override val groupId: Long
) : Event()

@Serializable
data class EventWithMessageEvent(
    override val type: String,
    @SerialName("object")
    val messageEvent: MessageEvent,
    @SerialName("group_id")
    override val groupId: Long
) : Event()

@Serializable
data class MessageEvent(
    @SerialName("user_id")
    val userId: Int,
    @SerialName("peer_id")
    val peerId: Int,
    @SerialName("event_id")
    val eventId: String
)


/**
 * Attachments to message.
 */

@Serializable
data class Attachment(val type: String)

@Serializable
data class MarketAttachment(val market: Market) {
    @Serializable
    data class Market(val category: Category) {
        @Serializable
        data class Category(val id: Int)
    }
}

@Serializable
data class Doc(
    val id: Int,
    //@SerialName("owner_id")
    val ownerId: Int
)
