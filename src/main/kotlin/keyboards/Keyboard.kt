package keyboards

import java.io.File
import java.io.InputStream

abstract class Keyboard {
    private val pathToDirectory = "src/main/resources/Keyboards"
    abstract val fileName: String

    fun getKeyboard(): String {
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