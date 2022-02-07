package keyboards

import java.io.File
import java.io.InputStream

abstract class Keyboard {
    private val pathToDirectory = "src/main/resources/keyboards"
    abstract var fileName: String

    open fun getKeyboard(): String {
        return try {
            val path = "$pathToDirectory/$fileName.json"
            val inputStream: InputStream = File(path).inputStream()
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            println(e.message)
            ""
        }
    }
}