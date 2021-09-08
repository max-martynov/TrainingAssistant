package keyboards

class ConfirmPaymentKeyboard() : Keyboard() {
    override val keyboard: String
        get() = """
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

}
