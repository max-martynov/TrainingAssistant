package keyboards

import kotlin.random.Random

class HowWasPlanKeyboard : Keyboard() {
    override var fileName: String = "HowWasPlan0"

    override fun getKeyboard(): String {
        fileName = "HowWasPlan" + (0..2).random().toString();
        return super.getKeyboard()
    }
}