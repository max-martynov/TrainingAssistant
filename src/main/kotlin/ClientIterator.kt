import kotlinx.coroutines.*
import org.h2.util.DateTimeUtils.getDayOfWeek
import java.lang.Thread.sleep
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

fun iterateOverClients(
    checkTime: LocalTime = LocalTime.of(18, 0),
    period: Duration = Duration.ofDays(1)
) = runBlocking {
    var nextCheckTime = checkTime
    //var cnt = 0L
    //var jobs: List<Job>
    //var clients: List<Client>

    while (true) {
        sleep(calculateDifference(nextCheckTime))
        println(Thread.activeCount())
        //clients = clientsRepository.getAll()
        //println(t.joinToString(" "))
        //println(clientsRepository.getAll().size)
        val clients = clientsRepository.getAll()
        println(clients)
        clients.forEach {
            launch {
                checkState(it)
            }
        }
        /*jobs = clientsRepository.getAll().map {
            launch {
                checkState(it)
            }
        }
        jobs.forEach { it.join() }*/
        nextCheckTime += period
    }
}

suspend fun checkState(client: Client) {
    //println("${LocalTime.now()}  ${client.status}  ${client.daysPassed}")
    val activeStatuses = listOf(Status.ACTIVE, Status.WAITING_FOR_START, Status.WAITING_FOR_RESULTS)
    if (activeStatuses.contains(client.status) && !client.trial) {
        if (client.daysPassed == 29) {
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
    }
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
