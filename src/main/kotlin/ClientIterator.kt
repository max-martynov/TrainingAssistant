import kotlinx.coroutines.delay
import org.h2.util.DateTimeUtils.getDayOfWeek
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ClientsIterator(
    private val clientRepository: ClientRepository,
    private val morningTime: LocalTime = LocalTime.of(9, 0),
    private val eveningTime: LocalTime = LocalTime.of(21, 0),
    private val nightTime: LocalTime = LocalTime.of(24, 0),
    private val period: Duration = Duration.ofDays(1)
) {

    var nextMorningTime = morningTime
    var nextEveningTime = eveningTime
    var nextNightTime = nightTime

    private var cnt = 0L

    suspend fun iterateMorning() {
        while (true) {
            delay(calculateDifference(nextMorningTime))
            println("${LocalDate.now().plusDays(cnt).dayOfWeek} - 09:00")
            clientRepository.getAllClients().forEach {
                updateMorning(it)
            }
            nextMorningTime += period
        }
    }

    private suspend fun updateMorning(client: Client) {
        if (client.status == Status.ACTIVE) {
            sendTraining(client)
            clientRepository.updateClient(
                client.id,
                newTotalDaysPassed = client.totalDaysPassed + 1,
                newDaysInWeekPassed = client.daysInWeekPassed + 1
            )
        } else if (client.status == Status.NEW_CLIENT) {
            clientRepository.updateClient(
                client.id,
                newStatus = Status.ACTIVE
            )
        } else if (client.status == Status.WAITING_FOR_RESULTS) {
            clientRepository.updateClient(
                client.id,
                newTotalDaysPassed = client.totalDaysPassed + 1
            )
        }
    }

    private suspend fun sendTraining(client: Client) =
        sendMessage(
            peerId = client.id,
            text = "Лови тренировку на сегодня:\n" +
                    client.trainingPlan.trainingDays[
                            LocalDate.now().plusDays(cnt).dayOfWeek.value - 1
                    ]
        ) // fix it, just for testing

    suspend fun iterateEvening() {
        while (true) {
            delay(calculateDifference(nextEveningTime))
            println("${LocalDate.now().plusDays(cnt).dayOfWeek} - 20:00")
            clientRepository.getAllClients().forEach {
                updateEvening(it)
            }
            nextEveningTime += period
        }
    }

    private suspend fun updateEvening(client: Client) {
        if (client.status == Status.ACTIVE) {
            if (client.totalDaysPassed == 30) {
                requestPayment(client)
            } else if (client.daysInWeekPassed == 7) { //end of week reached
                clientRepository.updateClient(
                    client.id,
                    newStatus = Status.WAITING_FOR_RESULTS
                )
                sendInterview(client)
            }
        }
    }

    /**
     * TODO - add correct implementation
     */

    private suspend fun requestPayment(client: Client) {
        clientRepository.updateClient(
            id = client.id,
            newStatus = Status.WAITING_FOR_PAYMENT
        )
        sendMessage(
            client.id,
            "Где деньги, Билли?"
        )
    }


    private suspend fun sendInterview(client: Client) {
        sendMessage(
            peerId = client.id,
            text = "Тренировочная неделя подошла к концу. " +
                    "Для продолжения заполните, пожалуйста, небольшой опрос, " +
                    "которой поможет определить план на следующую неделю.",
        )
        sendMessage(
            peerId = client.id,
            text = interview[0].question,
            keyboard = interview[0].answers
        )
    }

    suspend fun iterateNight() {
        while (true) {
            delay(calculateDifference(nextNightTime))
            println("${LocalDate.now().plusDays(++  cnt).dayOfWeek} - 00:00")
            clientRepository.getAllClients().forEach {
                updateNight(it)
            }
            nextNightTime += period
        }
    }

    private suspend fun updateNight(client: Client) {
        when (client.status) {
            Status.NEW_CLIENT -> {
                clientRepository.updateClient(
                    client.id,
                    newStatus = Status.ACTIVE
                )
            }
            Status.WAITING_FOR_RESULTS -> {
                if (client.interviewResults.size == interview.size) {
                    clientRepository.updateClient(
                        client.id,
                        newStatus = Status.ACTIVE,
                        newDaysInWeekPassed = 0,
                        newInterviewResults = mutableListOf()
                    )
                }
            }
            Status.WAITING_FOR_PAYMENT -> {
                if (client.totalDaysPassed == 0) {
                    clientRepository.updateClient(
                        client.id,
                        newStatus = Status.ACTIVE
                    )
                }
            }
            else -> {}
        }
    }

    private fun calculateDifference(requiredTime: LocalTime) =
        if (LocalTime.now().isBefore(requiredTime))
            LocalTime.now().until(requiredTime, ChronoUnit.MILLIS)
        else
            TimeUnit.DAYS.toMillis(1) - requiredTime.until(LocalTime.now(), ChronoUnit.MILLIS)
}