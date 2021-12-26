import java.io.File
import java.io.InputStream
import java.lang.StringBuilder


data class TrainingPlan(
    val activityType: Int,
    val duration: Int,
    val plan: String
)

class TrainingPlansRepository(
    private val pathToDirectory: String
) {
    fun getTrainingPlan(client: Client, activityType: Int, duration: Int): TrainingPlan {
        if (client.hasCompetition)
            return getTrainingPlanAfterCompetition(client)
        val builder = StringBuilder()
        val leadingPhrase = "Ваш план на эту неделю:\n\n"
        builder.append(leadingPhrase)
        for (i in 0 until 3) {
            builder.append(
                readStringFromFile(getRandomFile(activityType, duration, i))
            )
            builder.append("\n\n")
        }
        return TrainingPlan(activityType, duration, plan = builder.toString())
    }

    private fun getTrainingPlanAfterCompetition(client: Client): TrainingPlan {
        val builder = StringBuilder()
        val leadingPhrase = "Ваш план на эту неделю будет менее интенсивным, " +
                "однако, тренируюясь по нему, Вы сможете стартовать и на следующих выходных, если захотите:\n\n"
        builder.append(leadingPhrase)
        val activityType = 0
        val duration = client.trainingPlan.duration % 2
        val day = client.trainingPlan.duration / 10
        val path = "$pathToDirectory/$activityType/$duration/0_after_competition/$day.txt"
        builder.append(readStringFromFile(path))
        for (i in 1 until 3) {
            builder.append(
                readStringFromFile(getRandomFile(activityType, duration, i))
            )
            builder.append("\n\n")
        }
        return TrainingPlan(activityType, duration, plan = builder.toString())
    }

    private fun getRandomFile(activityType: Int, duration: Int, number: Int): String {
        val path = "$pathToDirectory/$activityType/$duration/$number"
        val sz = File(path).listFiles().size
        val id = (0 until sz).random()
        return "$path/$id.txt"
    }

    private fun readStringFromFile(path: String): String {
        val inputStream: InputStream = File(path).inputStream()
        return inputStream.bufferedReader().use { it.readText() }
    }
}

