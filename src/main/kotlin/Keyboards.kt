val pressStartKeyboard = """
            {
                "one_time":false,
                "buttons":[
                     [
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@-205462754-chat-bot-kratkoe-rukovodstvo",
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
                                "link":"https://vk.com/@-205462754-chat-bot-kratkoe-rukovodstvo",
                                "label":"Инструкция"
                            }
                        },
                        {
                            "action":{
                                "type":"open_link",
                                "link":"https://vk.com/@-205462754-chat-bot-kratkoe-rukovodstvo",
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

val paymentKeyboard = """
            {
                "one_time": false,
                "buttons": [
                    [
                        {
                            "action":{ 
                                "type":"vkpay", 
                                "hash":"action=pay-to-group&group_id=$groupId&amount=$paymentAmount&aid=7889001" 
                             } 
                        }
                    ]
                ],
                "inline": true
            }
        """.trimIndent()

