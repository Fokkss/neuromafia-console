package neuromafia.bot

interface HumanIo {
    fun writeLine(message: String)
    fun readLine(): String?
}

class ConsoleHumanIo : HumanIo {
    override fun writeLine(message: String) {
        println(message)
    }

    override fun readLine(): String? {
        return kotlin.io.readLine()
    }
}