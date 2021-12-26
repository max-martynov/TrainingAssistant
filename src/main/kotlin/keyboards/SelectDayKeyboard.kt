package keyboards


class SelectDayKeyboard : Keyboard() {
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
                            "label":"Суббота"
                        },
                        "color":"primary"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"Воскресенье"
                        },
                        "color":"primary"
                    }
                ]
            ],
            "inline":true
        }
        """.trimIndent()
}