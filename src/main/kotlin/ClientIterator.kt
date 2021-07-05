import kotlinx.coroutines.delay
import org.h2.util.DateTimeUtils.getDayOfWeek
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class ClientsIterator(
    private val clientRepository: ClientRepository,
    private val morningTime: LocalTime = LocalTime.of(9, 0),
    private val eveningTime: LocalTime = LocalTime.of(21, 0),
    private val period: Duration = Duration.ofDays(1)
) {

    var nextMorningTime = morningTime
    var nextEveningTime = eveningTime

    suspend fun iterateMorning() {
        while (true) {
            delay(calculateDifference(nextMorningTime))
            clientRepository.getAllClients().forEach {
                updateMorning(it)
            }
            nextMorningTime += period
        }
    }

    private suspend fun updateMorning(client: Client) {
        if (client.status == Status.ACTIVE) {
            sendTraining(client)
            client.totalDaysPassed++
            client.daysInWeekPassed++
        } else if (client.status == Status.NEW_CLIENT)
            client.status = Status.ACTIVE
    }

    private suspend fun sendTraining(client: Client) =
        sendMessage(
            peerId = client.id,
            text = "Лови тренировку на сегодня:\n" +
                    client.trainingPlan.trainingDays[LocalDate.now().dayOfWeek.value - 1]
        )

    suspend fun iterateEvening() {
        while (true) {
            delay(calculateDifference(nextEveningTime))
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
            }
            else if (client.daysInWeekPassed == 7) { //end of week reached
                sendInterview(client)
                client.status = Status.WAITING_FOR_RESULTS
            }
        }
    }

    private suspend fun requestPayment(client: Client) {
        sendMessage(
            peerId =
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
            text = "Как сам?\n1 - гроб, гроб, кладбище, 2 - ну такое, 3 - заебись",
            keyboard = """
                {
                    "one_time": false,
                    "buttons":
                    [
                        [
                            {
                                "action":{
                                "type":"text",
                                "label":"1"
                            },
                                "color":"primary"
                            }
                        ],
                        [
                            {
                                "action":{
                                "type":"text",
                                "label":"2"
                            },
                                "color":"primary"
                            }
                        ],
                        [
                            {
                                "action":{
                                "type":"text",
                                "label":"3"
                            },
                                "color":"primary"
                            }
                        ],
                ],
                "inline":true
            }
            """.trimIndent()
        )
    }

    private fun calculateDifference(requiredTime: LocalTime) =
        if (LocalTime.now().isBefore(requiredTime))
            LocalTime.now().until(requiredTime, ChronoUnit.MILLIS)
        else
            TimeUnit.DAYS.toMillis(1) - requiredTime.until(LocalTime.now(), ChronoUnit.MILLIS)


}