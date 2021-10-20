package eventHandlers

import Client
import ClientsRepository
import Status
import TrainingPlansRepository
import api.qiwi.QiwiApiClient
import api.vk.*
import com.petersamokhin.vksdk.core.model.event.IncomingMessage
import keyboards.PressStartKeyboard
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import stateHandlers.*
import java.lang.management.ManagementFactory


class IncomingMessageHandler(
    private val clientsRepository: ClientsRepository,
    private val vKApiClient: VKApiClient,
    private val trainingPlansRepository: TrainingPlansRepository,
    private val qiwiApiClient: QiwiApiClient
) {
    private val productId = 8 // 803 for Fake Community

    suspend fun receiveMessage(incomingMessage: IncomingMessage) {
        val clientId = incomingMessage.fromId
        val text = incomingMessage.text
        val attachments = incomingMessage.attachments

        println("Current number of threads = ${ManagementFactory.getThreadMXBean().threadCount}")

        val client = clientsRepository.findById(clientId)

        if (client == null && attachments.isNotEmpty() && isOurProduct(attachments[0].toString())) {
            registerNewClient(clientId)
        }
        else if (client != null) {
            getAppropriateHandler(client).handle(client, text)
        }
    }

    private fun isOurProduct(attachment: String): Boolean {
        return Json { ignoreUnknownKeys = true }.decodeFromString<Attachment>(attachment).type == "market" &&
                Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<MarketAttachment>(attachment).market.category.id == productId
    }

    private suspend fun registerNewClient(clientId: Int): Unit = coroutineScope {
        async { clientsRepository.add(Client(clientId)) }
        async { sendGreetings(clientId) }
    }

    private suspend fun sendGreetings(peerId: Int) {
        vKApiClient.sendMessageSafely(
            peerId,
            "Здравствуйте!\nСпасибо, что решили попробовать инновационные тренировки по подписке 🤖\n" +
                    "🔹 Если у Вас внизу не отображаются кнопки \"Старт\" и \"Инструкция\", нажмите на значок чуть правее поля для ввода сообещния.\n" +
                    "🔹 Если у Вас есть вопросы о том, как тут все работает, жмите \"Инструкция\".\n" +
                    "🔹 Если же Вы все поняли и готовы начинать, жмите \"Старт!\".",
            keyboard = PressStartKeyboard().keyboard
        )
    }

    private fun getAppropriateHandler(client: Client): StateHandler {
        return when(client.status) {
            Status.NEW_CLIENT -> NewClientHandler(clientsRepository, vKApiClient)
            Status.WAITING_FOR_PLAN -> WaitingForPlanHandler(clientsRepository, vKApiClient)
            Status.WAITING_FOR_START -> WaitingForStartHandler(clientsRepository, vKApiClient, trainingPlansRepository)
            Status.ACTIVE -> ActiveClientHandler(clientsRepository, vKApiClient)
            Status.WAITING_FOR_RESULTS -> WaitingForResultsHandler(clientsRepository, vKApiClient, trainingPlansRepository, qiwiApiClient)
            else -> WaitingForPaymentHandler(clientsRepository, vKApiClient)
        }
    }
}


/**
 * 1. Скажите, пожалуйста, как Ваше самочувствие после пройденного недельного цикла?
 *  - Устал / утомился
 *  - Чувствую себя нормально
 *  - Чувствую себя легко
 * 2. Нужно ли сделать тренировочный план легче / меньше?
 *  - Да
 *  - Нет
 * 3. Болели ли Вы в течение недельного цикла?
 *  - Да
 *  - Нет
 * 4. Нужно ли вам восстановление? (в случае ответа Нет на предыдущий вопрос, тут автоматически ответ Нет)
 *  - Да
 *  - Нет
 *
 *
 * Постоянная кнопка: Начал выполнение недельног оцикла, Закончил выаплнеие недельного циклва Обратная связь
 * Деньги собирать всегда через 30 дней
 * Может начать план только 4 раза за месяц
 * Забиваем на 3 часа
 *
 * "action": {
"type": "vkpay",
"payload": "{\"button\": \"1\"}",
"hash": "action=pay-to-user&amount=5&description=aaaa&user_id=15733972&aid=7889001"
}
 *
 *  - Пробная неделя
 *  - Добавить постоянную кнопку промокоды
 *  - 29 июля
 *
 *
 */
