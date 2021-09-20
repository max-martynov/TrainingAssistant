package keyboards

class MainKeyboardBeforePayment() : Keyboard() {
    override val keyboard: String
        get() = """
            {
                "one_time":false,
                "buttons":[
                     [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.me/tuchin_a_95",
                                "label":"Обратная связь"
                            }
                        } 
                     ], [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@club_prosto_o_trenirovkah-trenirovki-po-podpiske-20",
                                "label":"Инструкция"
                            }
                        }
                     ], [   
                        {
                            "action":{
                                "type":"text",
                                "label":"Начать цикл"
                            },
                            "color":"primary"
                        }, 
                        {
                            "action":{
                                "type":"text",
                                "label":"Закончить цикл"
                            },
                            "color":"primary"
                        }
                    ]
                ],
                "inline": false
            }
        """.trimIndent()
}