import ApiClients.VKApiClient
import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ClientIterator(
    private val clientsRepository: ClientsRepository,
    private val qiwiApiClient: QiwiApiClient
) {
    suspend fun iterateOverClients(
        checkTime: LocalTime = LocalTime.of(9, 0),
        period: Duration = Duration.ofDays(1)
    ) {
        var nextCheckTime = checkTime

        val vkApiClient = VKApiClient()

        while (true) {
            delay(calculateDifference(nextCheckTime))
            printInfo()
            val clients = clientsRepository.getAll()
            println("Total number of clients = ${clients.size}\n")
            coroutineScope {
                val numberActiveClients = AtomicInteger(0)
                val jobs = clients.map {
                    launch {
                        numberActiveClients.addAndGet(checkState(it, vkApiClient))
                    }
                }
                jobs.forEach { it.join() }
                println("\nNumber of active clients = $numberActiveClients")
            }
            println("\n------------------------------------------------------\n")
            nextCheckTime += period
        }
    }

    suspend fun checkState(client: Client, vkApiClient: VKApiClient): Int {
        println(client)
        val activeStatuses = listOf(Status.ACTIVE, Status.WAITING_FOR_START, Status.WAITING_FOR_RESULTS)
        if (activeStatuses.contains(client.status) && !client.trial) {
            if (client.daysPassed == 28) {
                clientsRepository.update(
                    client.id,
                    newStatus = Status.WAITING_FOR_PAYMENT
                )
                requestPaymentToContinue(client, vkApiClient)
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

    suspend fun requestPaymentToContinue(client: Client, vkApiClient: VKApiClient) {
        val phrases = listOf(
            "К сожалению, месячная подписка истекла! Продлите ее, если Вам понравился тренировочный процесс.",
            "К сожалению, месячная подписка истекла! Но Вы можете продлить ее, чтобы продолжить тренировочный процесс.",
        )
        client.updateBill()
        vkApiClient.sendMessageSafely(
            client.id,
            phrases.random(),
            keyboard = getPaymentKeyboard(qiwiApiClient.getPayUrl(client.billId))
        )
    }

    private fun printInfo() {
        println("${LocalDate.now()}\nCurrent number of threads = ${ManagementFactory.getThreadMXBean().threadCount}\n")
    }

    private fun calculateDifference(requiredTime: LocalTime) =
        if (LocalTime.now().isBefore(requiredTime))
            LocalTime.now().until(requiredTime, ChronoUnit.MILLIS)
        else
            TimeUnit.DAYS.toMillis(1) - LocalTime.of(0, 0).until(LocalTime.now(), ChronoUnit.MILLIS) +
                    LocalTime.of(0, 0).until(requiredTime, ChronoUnit.MILLIS)


}


