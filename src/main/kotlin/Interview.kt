data class InterviewButton(
    val answer: String,
    val color: String = "primary"
) {
    override fun toString(): String {
        return """
            {
                "action": {
                    "type": "text",
                    "label": "$answer"
                },
                "color": "$color"
            }
        """.trimIndent()
    }
}

data class InterviewQuestion(val question: String, val interviewButtons: List<InterviewButton>) {
    override fun toString(): String {
        if (interviewButtons.size == 2) {
            return  """
                {
                    "one_time": false,
                    "buttons":
                    [
                        [
                            ${interviewButtons[0]},
                            ${interviewButtons[1]}
                        ]
                    ],
                    "inline":true
            }
            """.trimIndent()
        }
        else {
            var res =  """
                {
                    "one_time": false,
                    "buttons":
                    [
            """.trimIndent()
            interviewButtons.forEach {
                res += "[$it]"
                if (it != interviewButtons.last())
                    res += ","
                else
                    res += """
                        ],
                    "inline":true
               }
                    """.trimIndent()
            }
            return res
        }
    }
}

object Interview {
    val interviewQuestions = listOf(
        InterviewQuestion(
            "Скажите, пожалуйста, как Ваше самочувствие после пройденного недельного цикла?",
            listOf(
                InterviewButton("Устал / утомился"),
                InterviewButton("Чувствую себя нормально"),
                InterviewButton("Чувствую себя легко")
            )
        ),
        InterviewQuestion(
            "Нужно ли сделать тренировочный план легче / меньше?",
            listOf(
                InterviewButton("Да"),
                InterviewButton("Нет")
            )
        ),
        InterviewQuestion(
            "Болели ли Вы в течение недельного цикла?",
            listOf(
                InterviewButton("Да"),
                InterviewButton("Нет")
            )
        ),
        InterviewQuestion(
            "Нужно ли вам восстановление?",
            listOf(
                InterviewButton("Да"),
                InterviewButton("Нет")
            )
        )
    )

    fun findAnswerNumberOnKthQuestion(answer: String, k: Int): Int {
        interviewQuestions[k].interviewButtons.forEachIndexed { index, interviewButton ->
            if (interviewButton.answer == answer)
                return@findAnswerNumberOnKthQuestion index
        }
        return -1
    }
}

