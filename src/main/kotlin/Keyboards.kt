val pressStartKeyboard = """
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

val mainKeyboard = """
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
                        },
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@club_prosto_o_trenirovkah-skidki-dlya-uchastnikov-kluba",
                                "label":"Промокоды"
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

val selectHoursKeyboard = """
        {
            "one_time": false, 
            "buttons":
            [ 
                [ 
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"6 часов"
                        },
                        "color":"primary"
                    },
                    { 
                        "action":{ 
                            "type":"text", 
                            "label":"10 часов"
                        },
                        "color":"primary"
                    }
                ]
            ],
            "inline":true
        }
    """.trimIndent()

fun getPaymentKeyboard(link: String): String =
     """
            {
                "one_time": false,
                "buttons": [
                    [
                        {
                            "action":{ 
                                "type": "open_link", 
                                "link": "$link",
                                "label": "Оплатить подписку"
                             } 
                        }
                    ], [
                        {
                            "action":{ 
                                "type": "callback", 
                                "label": "Подтвердить оплату"
                             },
                             "color": "positive"
                        }
                    ]
                ],
                "inline": true
            }
        """.trimIndent()

val confirmPaymentKeyboard =
     """
            {
                "one_time": false,
                "buttons": [
                    [
                        {
                            "action":{ 
                                "type": "callback", 
                                "label": "Подтвердить оплату"
                             },
                             "color": "positive"
                        }
                    ]
                ],
                "inline": true
            }
        """.trimIndent()
