package neuromafia.dev

object DevLog {
    var enabled: Boolean = false

    fun info(message: String) {
        if (enabled) {
            println("[DEV] $message")
        }
    }
}