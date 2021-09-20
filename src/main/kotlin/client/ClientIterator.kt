package client

import Client
import ClientsRepository
import Status
import api.qiwi.QiwiApiClient
import api.vk.VKApiClient
import keyboards.PaymentKeyboard
import kotlinx.coroutines.*
import java.lang.management.ManagementFactory
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ClientIterator(
    private val clientsRepository: ClientsRepository,
    private val vkApiClient: VKApiClient,
    private val qiwiApiClient: QiwiApiClient
) {
    private val activeStatuses = listOf(Status.ACTIVE, Status.WAITING_FOR_START, Status.WAITING_FOR_RESULTS)

    suspend fun iterateOverClients(
        checkTime: LocalTime = LocalTime.of(9, 0),
        period: Duration = Duration.ofDays(1)
    ) {
        var nextCheckTime = checkTime

        while (true) {
            delay(calculateDifference(nextCheckTime))
            val clients = clientsRepository.getAll()
            coroutineScope {
                val jobs = clients.map {
                    launch {
                        checkState(it, vkApiClient)
                    }
                }
                jobs.forEach { it.join() }
            }
            logAllClients()
            nextCheckTime += period
        }
    }

    private fun calculateDifference(requiredTime: LocalTime) =
        if (LocalTime.now().isBefore(requiredTime))
            LocalTime.now().until(requiredTime, ChronoUnit.MILLIS)
        else
            TimeUnit.DAYS.toMillis(1) - LocalTime.of(0, 0).until(LocalTime.now(), ChronoUnit.MILLIS) +
                    LocalTime.of(0, 0).until(requiredTime, ChronoUnit.MILLIS)

    private suspend fun checkState(client: Client, vkApiClient: VKApiClient) {
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
        }
    }

    private suspend fun requestPaymentToContinue(client: Client, vkApiClient: VKApiClient) {
        val phrases = listOf(
            "Месячная подписка истекла! Продлите ее, если Вам понравился тренировочный процесс и Вы бы хотели продолжить двигаться к своей цели!\n" +
                    "Для того, чтобы продлить подписку, нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\"",
            "Месячная подписка истекла! Но Вы можете продлить ее, чтобы продолжить тренироваться по персональным планам!\n" +
                    "Для того, чтобы продлить подписку, нажмите \"Оплатить подписку\", а после завершения платежа - \"Подтвердить оплату\"",
        )
        qiwiApiClient.updateBill(client, clientsRepository)
        vkApiClient.sendMessageSafely(
            client.id,
            phrases.random(),
            keyboard = PaymentKeyboard(qiwiApiClient.getPayUrl(client.billId)).keyboard
        )
    }

    private suspend fun logAllClients() {
        val clients = clientsRepository.getAll()
        println("\n\nLOG OF ALL CLIENTS ${LocalDateTime.now()}\n" +
                "Total number of registered clients: ${clients.size}\n")
        val numberOfActiveClients = clients.count { (it.status in activeStatuses) && (!it.trial) }
        println("Total number of active clients: $numberOfActiveClients\n\nList of all clients:\n")
        clients.forEach { println(it) }
        println("Current number of threads = ${ManagementFactory.getThreadMXBean().threadCount}\n")
        println("------------------------------------\n")
    }
}


