package neuromafia.core.engine

import neuromafia.core.model.GameState
import neuromafia.core.model.Winner
import neuromafia.core.model.Phase
import neuromafia.core.model.GameEvent


object WinConditionChecker {
    fun checkWinner(state: GameState): Winner? {
        val aliveMafiaCount = state.aliveMafiaPlayers().size
        val aliveCivilianCount = state.aliveCivilianTeamPlayers().size

        if (aliveMafiaCount == 0) {
            return Winner.CIVILIANS
        }

        if (aliveMafiaCount >= aliveCivilianCount) {
            return Winner.MAFIA
        }

        return null
    }

    fun updateState(state: GameState): GameState {
        val winner = checkWinner(state)

        return if (winner == null) {
            state
        } else {
            state.copy(
                phase = Phase.FINISHED,
                winner = winner,
                eventLog = state.eventLog + GameEvent.WinnerDeclared(winner)
            )
        }
    }
}