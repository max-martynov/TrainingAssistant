import keyboards.Keyboard

class ReviewKeyboard() : Keyboard() {
    override val keyboard: String = """
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
                        "color":"negative"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"2"
                        },
                        "color":"negative"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"3"
                        },
                        "color":"primary"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"4"
                        },
                        "color":"positive"
                    }, { 
                        "action":{ 
                            "type":"text", 
                            "label":"5"
                        },
                        "color":"positive"
                    }
                ]
            ],
            "inline":true
        }
    """.trimIndent()
}