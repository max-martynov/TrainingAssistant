package keyboards

class PrimaryActivityKeyboard() : Keyboard() {
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
                            "label":"Лыжи, 6 часов в неделю"
                        },
                        "color":"primary"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"Лыжи, 10 часов в неделю"
                        },
                        "color":"primary"
                    }
                ], 
                [
                    {
                        "action":{ 
                            "type":"text", 
                            "label":"Бег, 6 часов в неделю"
                        },
                        "color":"primary"
                    },
                    {
                        "action":{ 
                            "type":"text", 
                            "label":"Бег, 10 часов в неделю"
                        },
                        "color":"primary"
                    }
                ], 
                [ 
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"ОФП, 4 часа в неделю"
                        },
                        "color":"primary"
                    }
                ]
            ],
            "inline":true
        }
        """.trimIndent()
}