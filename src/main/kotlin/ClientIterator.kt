import kotlinx.coroutines.delay
import org.h2.util.DateTimeUtils.getDayOfWeek
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

suspend fun iterateOverClients(
    checkTime: LocalTime = LocalTime.of(18, 0),
    period: Duration = Duration.ofDays(1)
) {
    var nextCheckTime = checkTime
    var cnt = 0L

    while (true) {
        delay(calculateDifference(nextCheckTime))
        println(clientsRepository.getAll().size)
        clientsRepository.getAll().forEach {
            checkState(it)
        }
        nextCheckTime += period
    }
}

suspend fun checkState(client: Client) {
    println("${LocalTime.now()}  ${client.status}  ${client.daysPassed}")
    val activeStatuses = listOf(Status.ACTIVE, Status.WAITING_FOR_START, Status.WAITING_FOR_RESULTS)
    if (activeStatuses.contains(client.status)) {
        if (client.daysPassed == 29) {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_PAYMENT
            )
            requestPaymentToContinue(client.id, amount = 1)
        }
        else {
            clientsRepository.update(
                client.id,
                newDaysPassed = client.daysPassed + 1
            )
        }
    }
}

suspend fun requestPaymentToContinue(peerId: Int, toGroup: Int = groupId, amount: Int = 500) {
    val phrases = listOf(
        "К сожалению, месячная подписка истекла! Продлите ее, если Вам понравился тренировочный процесс.",
        "К сожалению, месячная подписка истекла! Но Вы можете продлить ее, чтобы продолжить тренировочный процесс.",
    )
    sendMessage(
        peerId,
        phrases.random(),
        keyboard = """
            {
                "one_time": false,
                "buttons": [
                    [
                        {
                            "action": {
                                "type": "vkpay",
                                "hash": "action=pay-to-group&amount=$amount&group_id=$toGroup&aid=7889001"
                            }
                        }
                    ]
                ],
                "inline": true
            }
        """.trimIndent()
    )
}

private fun calculateDifference(requiredTime: LocalTime) =
    if (LocalTime.now().isBefore(requiredTime))
        LocalTime.now().until(requiredTime, ChronoUnit.MILLIS)
    else
        TimeUnit.DAYS.toMillis(1) - requiredTime.until(LocalTime.now(), ChronoUnit.MILLIS)
