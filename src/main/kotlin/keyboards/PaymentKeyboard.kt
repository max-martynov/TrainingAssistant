package keyboards

class PaymentKeyboard(private val link: String) : Keyboard() {
    override val keyboard: String
        get() = """
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
}