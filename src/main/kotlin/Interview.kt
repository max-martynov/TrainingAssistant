data class InterviewQuestion(val question: String, val answers: String)

val interview = listOf(
    InterviewQuestion(
        "Как состояние?\n1 - помянем, 2 - с пивом покатит, 3 - превосходно",
        """
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
                            },
                            {
                                "action":{
                                    "type":"text",
                                    "label":"2"
                                },
                                "color":"primary"
                            },
                            {
                                "action":{
                                    "type":"text",
                                    "label":"3"
                                },
                                "color":"primary"
                            }
                        ]
                    ],
                    "inline":true
            }
            """.trimIndent()
    ), InterviewQuestion(
        "Выбери любимую цифру",
        """
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
                                "color":"secondary"
                            },
                            {
                                "action":{
                                    "type":"text",
                                    "label":"2"
                                },
                                "color":"negative"
                            },
                            {
                                "action":{
                                    "type":"text",
                                    "label":"3"
                                },
                                "color":"positive"
                            }
                        ]
                    ],
                    "inline":true
            }
            """.trimIndent()
    )
)
