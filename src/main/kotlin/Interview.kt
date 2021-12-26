//data class InterviewButton(
//    val answer: String,
//    val color: String = "primary"
//) {
//    override fun toString(): String {
//        return """
//            {
//                "action": {
//                    "type": "text",
//                    "label": "$answer"
//                },
//                "color": "$color"
//            }
//        """.trimIndent()
//    }
//}
//
//data class InterviewQuestion(val question: String, val interviewButtons: List<InterviewButton>) {
//    override fun toString(): String {
//        if (interviewButtons.size == 2) {
//            return  """
//                {
//                    "one_time": false,
//                    "buttons":
//                    [
//                        [
//                            ${interviewButtons[0]},
//                            ${interviewButtons[1]}
//                        ]
//                    ],
//                    "inline":true
//            }
//            """.trimIndent()
//        }
//        else if (interviewButtons.size == 5) {
//            return  """
//                {
//                    "one_time": false,
//                    "buttons":
//                    [
//                        [
//                            ${interviewButtons[0]},
//                            ${interviewButtons[1]},
//                            ${interviewButtons[2]},
//                            ${interviewButtons[3]},
//                            ${interviewButtons[4]}
//                        ]
//                    ],
//                    "inline":true
//            }
//            """.trimIndent()
//        } else {
//            var res =  """
//                {
//                    "one_time": false,
//                    "buttons":
//                    [
//            """.trimIndent()
//            interviewButtons.forEach {
//                res += "[$it]"
//                if (it != interviewButtons.last())
//                    res += ","
//                else
//                    res += """
//                        ],
//                    "inline":true
//               }
//                    """.trimIndent()
//            }
//            return res
//        }
//    }
//}
//
//abstract class Interview {
//    abstract val interviewQuestions: List<InterviewQuestion>
//
//    protected val reviewQuestion = InterviewQuestion(
//        "Оцените, пожалуйста, пройденный план по 5-бальной шкале (1 - не понравился, 5 - превосходно). Нам важно Ваше мнение!",
//        listOf(
//            InterviewButton("1", "negative"),
//            InterviewButton("2", "negative"),
//            InterviewButton("3", "primary"),
//            InterviewButton("4", "positive"),
//            InterviewButton("5", "positive"),
//        )
//    )
//
//    fun findAnswerNumberOnKthQuestion(answer: String, k: Int): Int {
//        interviewQuestions[k].interviewButtons.forEachIndexed { index, interviewButton ->
//            if (interviewButton.answer == answer)
//                return@findAnswerNumberOnKthQuestion index
//        }
//        return -1
//    }
//
//}
//
//class InterviewFor1Hour : Interview() {
//    override val interviewQuestions = listOf(
//        InterviewQuestion(
//            "Скажите, пожалуйста, как Ваше самочувствие после пройденного недельного цикла?",
//            listOf(
//                InterviewButton("Устал / утомился"),
//                InterviewButton("Чувствую себя нормально"),
//                InterviewButton("Чувствую себя легко")
//            )
//        ),
//        InterviewQuestion(
//            "Восстановились ли Вы после болезни?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//        InterviewQuestion(
//            "Сколько часов Вы бы хотели заниматься на следующей неделе?",
//            listOf(
//                InterviewButton("6 часов"),
//                InterviewButton("10 часов")
//            )
//        ),
//        reviewQuestion
//    )
//}
//
//class InterviewFor6Hours : Interview() {
//    override val interviewQuestions = listOf(
//        InterviewQuestion(
//            "Скажите, пожалуйста, как Ваше самочувствие после пройденного недельного цикла?",
//            listOf(
//                InterviewButton("Устал / утомился"),
//                InterviewButton("Чувствую себя нормально"),
//                InterviewButton("Чувствую себя легко")
//            )
//        ),
//        InterviewQuestion(
//            "Нужно ли сделать тренировочный план сложнее / больше?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//        InterviewQuestion(
//            "Болели ли Вы в течение недельного цикла?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//        InterviewQuestion(
//            "Нужно ли вам восстановление?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//        reviewQuestion
//    )
//}
//
//class InterviewFor10Hours : Interview() {
//    override val interviewQuestions = listOf(
//        InterviewQuestion(
//            "Скажите, пожалуйста, как Ваше самочувствие после пройденного недельного цикла?",
//            listOf(
//                InterviewButton("Устал / утомился"),
//                InterviewButton("Чувствую себя нормально"),
//                InterviewButton("Чувствую себя легко")
//            )
//        ),
//        InterviewQuestion(
//            "Нужно ли сделать тренировочный план легче / меньше?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//        InterviewQuestion(
//            "Болели ли Вы в течение недельного цикла?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//        InterviewQuestion(
//            "Нужно ли вам восстановление?",
//            listOf(
//                InterviewButton("Да"),
//                InterviewButton("Нет")
//            )
//        ),
//         reviewQuestion
//    )
//}
//
//
