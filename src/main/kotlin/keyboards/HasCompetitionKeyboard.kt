package keyboards

class HasCompetitionKeyboard : Keyboard() {
    override val keyboard: String
        get() = """
            {
            "one_time": false, 
            "buttons":
            [ 
                [ 
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"Да"
                        },
                        "color":"positive"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"Нет"
                        },
                        "color":"negative"
                    }
                ]
            ],
            "inline":true
        }
        """.trimIndent()
}