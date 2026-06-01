package neuromafia.dev

import neuromafia.core.model.ConsoleColors

// basic dev logger
object DevLog {
    var enabled: Boolean = false

    fun info(message: String) {
        if (enabled) {
            println("${ConsoleColors.YELLOW}[DEV] $message${ConsoleColors.RESET}")
            System.out.flush()
        }
    }
}