import kotlinx.coroutines.*
import org.h2.util.DateTimeUtils.getDayOfWeek
import java.lang.Thread.sleep
import java.lang.management.ManagementFactory
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

fun iterateOverClients(
    checkTime: LocalTime = LocalTime.of(18, 0),
    period: Duration = Duration.ofDays(1)
) = runBlocking {
    var nextCheckTime = checkTime

    while (true) {
        sleep(calculateDifference(nextCheckTime))
        printCurrentNumberOfThreads()
        val clients = clientsRepository.getAll()
        println("Total number of clients: ${clients.size}")
        val numberActiveClients = AtomicInteger(0)
        val jobs = clients.map {
            launch {
                numberActiveClients.addAndGet(checkState(it))
            }
        }
        jobs.forEach { it.join() }
        println("Number of active clients: $numberActiveClients")
        nextCheckTime += period
    }
}

suspend fun checkState(client: Client): Int {
    println(client)
    val activeStatuses = listOf(Status.ACTIVE, Status.WAITING_FOR_START, Status.WAITING_FOR_RESULTS)
    if (activeStatuses.contains(client.status) && !client.trial) {
        if (client.daysPassed == 28) {
            clientsRepository.update(
                client.id,
                newStatus = Status.WAITING_FOR_PAYMENT
            )
            requestPaymentToContinue(client.id)
        } else {
            clientsRepository.update(
                client.id,
                newDaysPassed = client.daysPassed + 1
            )
        }
        return 1
    }
    return 0
}

suspend fun requestPaymentToContinue(peerId: Int) {
    val phrases = listOf(
        "К сожалению, месячная подписка истекла! Продлите ее, если Вам понравился тренировочный процесс.",
        "К сожалению, месячная подписка истекла! Но Вы можете продлить ее, чтобы продолжить тренировочный процесс.",
    )
    sendMessage(
        peerId,
        phrases.random(),
        keyboard = paymentKeyboard
    )
}

private fun calculateDifference(requiredTime: LocalTime) =
    if (LocalTime.now().isBefore(requiredTime))
        LocalTime.now().until(requiredTime, ChronoUnit.MILLIS)
    else
        TimeUnit.DAYS.toMillis(1) - requiredTime.until(LocalTime.now(), ChronoUnit.MILLIS)
