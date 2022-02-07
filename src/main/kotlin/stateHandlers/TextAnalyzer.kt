package stateHandlers

import api.vk.VKApiClient
import client.Client
import keyboards.YesNoKeyboard

enum class Answer {
    GOOD, NORMAL, HARD
}

data class Answers(
    val good: List <String>,
    val normal: List <String>,
    val hard: List <String>
)

object TextAnalyzer {

    private val answersToFirstQuestion = Answers(
        good = listOf("Довольно легко", "Есть легкость"),
        normal = listOf( "Нормальный уровень", "В целом нормально"),
        hard = listOf("Тяжеловато", "Было тяжело")
    )
    private val clientAnswer = mutableMapOf<Client, Answer?>()

    suspend fun processText(client: Client, text: String, vkApiClient: VKApiClient): String? {
        val answer = clientAnswer[client]
        if (answer != null) {
            if (text == "Да" || text == "Нет") {
                val str = analyzeText(client, answer, text == "Да")
                clientAnswer[client] = null
                return str
            }
            else {
                repeatQuestion(client, vkApiClient)
                return null
            }
        } else {
            if (answersToFirstQuestion.good.contains(text))
                clientAnswer[client] = Answer.GOOD
            else if (answersToFirstQuestion.normal.contains(text))
                clientAnswer[client] = Answer.NORMAL
            else if (answersToFirstQuestion.hard.contains(text))
                clientAnswer[client] = Answer.HARD
            else {
                repeatQuestion(client, vkApiClient)
                return null
            }
            vkApiClient.sendMessageSafely(
                client.id,
                "Болели ли Вы за последнюю неделю? Или возможно чувствовали слабость или недомогание?",
                keyboard = YesNoKeyboard().getKeyboard()
            )
            return null
        }
    }

    private suspend fun repeatQuestion(client: Client, vkApiClient: VKApiClient) {
        vkApiClient.sendMessageSafely(
            client.id,
            "Выберите, пожалуйста, один из предложенных вариантов ответа."
        )
    }

    private fun analyzeText(client: Client, answer: Answer, isIll : Boolean): String {
        val answers = mutableListOf<String>()
        if (isIll) {
            answers.add("Сейчас Вы скорее всего не готовы к серьезным тренировкам, поэтому рекомендую перейти на восстановительный план.")
            if (client.trainingPlan.duration == 1) {
                answers.add("Рекомендую Вам снизить нагрузку до 6 часов, а лучше перейти на восстановительный план, ведь он разрабатывался специально для этих целей.")
            } else {
                answers.add("В таком состоянии лучше не рисковать, поэтому рекомендую Вам пройти восстановительную неделю!")
            }
        } else {
            if (client.trainingPlan.activityType == 3) {
                if (answer == Answer.GOOD || answer == Answer.NORMAL)
                    return "Восстановлние сработало! Рекомендую Вам возвращаться к обычному режиму тренировок."
                else
                    return "Я бы порекомендовал Вам еще одну восстановительную неделю."
            }
            when(answer) {
                Answer.GOOD -> {
                    answers.add("Отличный результат! Рекомендую придерживайтесь такого же плана.")
                    if (client.trainingPlan.duration == 1) {
                        answers.add("Вы в отличной форме! Скорее всего не надо ничего менять, все идет по плану.")
                    } else if (client.trainingPlan.activityType == 2) {
                        answers.add("Вы в потрясающей форме! Если Вы готовы к более серьезным занятиям, попробуйте беговой план на 6 часов.")
                    } else {
                        answers.add("Вы в потрясающей форме! Продолжайте в том же духе, либо попробуйте перейти на 10-часовой план.")
                    }
                }
                Answer.NORMAL -> {
                    answers.add("Хороший результат! Наверное стоит придерживаться такого же плана.")
                    answers.add("В целом, это правильное тренировочное состояние, поэтому я бы порекомендовал Вам выбрать такой же план.")
                    answers.add("Ваш план подходит Вам идеально! Я бы порекомдовал Вам выбрать такой же, чтобы сохранить тренировочный эффект.")
                }
                Answer.HARD -> {
                    if (client.trainingPlan.duration == 1) {
                        answers.add("Скорее всего Вы переутомились, поэтому я бы посоветовал Вам хотя бы на одну неделю перейти на 6-часовой план.")
                        answers.add("В таком состоянии лучше всего уменьшить объемы и попробовать 6-часовой план.")
                    } else if (client.trainingPlan.activityType == 2) {
                        answers.add("Это была непростая неделя. Я бы порекомендовал Вам выбрать такой же план, но выполнять его чуть менее активно.")
                        answers.add("Рекомендую Вам выбрать такой же план, но чуть больше отдыхать, либо выбрать Восстановление, чтобы лучше разгрузиться.")
                    } else {
                        answers.add("Возможно Вам стоит попробовать ОФП план. С помошью него Вы сможете разгрузиться и вернуть тренировочную легкость.")
                        answers.add("Да, это была непростая неделя. Если чувствуете в себе силы, выбирайте такой же план. Либо можете попробовать неделю ОФП.")
                    }
                }
            }
        }
        return answers.random()
    }
}