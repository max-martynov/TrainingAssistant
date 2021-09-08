package keyboards

class PressStartKeyboard() : Keyboard() {
    override val keyboard: String
        get() = """
            {
                "one_time":false,
                "buttons":[
                     [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@club_prosto_o_trenirovkah-trenirovki-po-podpiske-20",
                                "label":"Инструкция"
                            }
                        }, {
                            "action":{
                                "type":"text",
                                "label":"Старт!"
                            },
                            "color":"primary"
                        }
                     ]
                ],
                "inline": false
            }
        """.trimIndent()
}