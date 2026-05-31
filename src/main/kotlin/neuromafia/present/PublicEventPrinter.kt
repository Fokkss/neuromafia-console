package neuromafia.present

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.msg.Language

class PublicEventPrinter(
    private val language: Language
) {
    private val formatter = PublicEventFormatter(language)

    fun printHeader() {
        when (language) {
            Language.EN -> println("Public game log:")
            Language.RU -> println("Публичный лог игры:")
        }

        System.out.flush()
    }

    fun printPublicEvents(state: GameState) {
        println("")
        printHeader()

        state.eventLog.forEach { event ->
            printPublicEvent(event)
        }
    }

    fun printPublicEvent(event: GameEvent) {
        val line = formatter.formatEvent(event)

        if (line != null) {
            println("  $line")
            System.out.flush()
        }
    }

    fun printNewPublicEvents(
        previousState: GameState,
        currentState: GameState
    ) {
        val newEvents = currentState.eventLog.drop(previousState.eventLog.size)

        newEvents.forEach { event ->
            printPublicEvent(event)
        }
    }

    fun printGameSummary(state: GameState) {
        println("")

        formatter.formatSummary(state).forEach { line ->
            println(line)
        }
    }
}