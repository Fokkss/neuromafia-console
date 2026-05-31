package neuromafia.present

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Winner

class PublicEventPrinter {
    fun printGameSummary(state: GameState) {
        println("")
        println("Game finished: ${state.finished}")
        println("Current day: ${state.dayNumber}")
        println("Current phase: ${state.phase}")
        println("Winner: ${formatWinner(state.winner)}")
        println("Alive players: ${state.alivePlayers().map { it.id }}")
    }

    fun printPublicEvents(state: GameState) {
        println("")
        println("Public game log:")

        state.eventLog.forEach { event ->
            val line = publicLine(event)

            if (line != null) {
                println("  $line")
            }
        }
    }

    private fun publicLine(event: GameEvent): String? {
        return when (event) {
            is GameEvent.PlayerSpoke ->
                "Player ${event.playerId} spoke."

            is GameEvent.PlayerNominated ->
                "Player ${event.speakerId} nominated Player ${event.nominatedPlayerId}."

            is GameEvent.PlayerVoted ->
                if (event.targetId == null) {
                    "Player ${event.voterId} skipped voting."
                } else {
                    "Player ${event.voterId} voted."
                }

            is GameEvent.DayVotingTie ->
                "Day voting ended with a tie."

            is GameEvent.PlayerKilled ->
                "A player was killed."

            is GameEvent.PlayerMuted ->
                "A player cannot speak and vote today."

            is GameEvent.PhaseChanged ->
                phaseLine(event.from, event.to, event.dayNumber)

            is GameEvent.WinnerDeclared ->
                "Game finished. Winner: ${formatWinner(event.winner)}."

            is GameEvent.MafiaKillVoteRecorded ->
                null

            is GameEvent.MafiaKillTargetSelected ->
                null

            is GameEvent.MafiaKillTie ->
                null

            is GameEvent.GodfatherKillDecisionMade ->
                null

            is GameEvent.GodfatherCommissarChecked ->
                null

            is GameEvent.DoctorProtected ->
                null

            is GameEvent.CommissarChecked ->
                null

            is GameEvent.EscortVisited ->
                null

            is GameEvent.ManiacKillTargetSelected ->
                null
        }
    }

    private fun phaseLine(
        from: Phase,
        to: Phase,
        dayNumber: Int
    ): String {
        return "Phase changed: $from -> $to, day $dayNumber."
    }

    private fun formatWinner(winner: Winner?): String {
        return when (winner) {
            Winner.CIVILIANS -> "civilians"
            Winner.MAFIA -> "mafia"
            null -> "none"
        }
    }
}