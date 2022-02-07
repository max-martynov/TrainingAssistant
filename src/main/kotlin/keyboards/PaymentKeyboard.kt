package keyboards

class PaymentKeyboard : Keyboard() {
    override var fileName: String = ""

    fun getKeyboard(link: String): String {
        return """
            {
              "one_time": false,
              "buttons": [
                [
                  {
                    "action": {
                      "type": "open_link",
                      "link": "$link",
                      "label": "Оплатить подписку"
                    }
                  }
                ],
                [
                  {
                    "action": {
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
}