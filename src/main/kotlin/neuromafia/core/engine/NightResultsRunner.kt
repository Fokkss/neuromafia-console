package neuromafia.core.engine

import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Role
import neuromafia.dev.DevLog

object NightResultsRunner {
    fun resolveNightKills(state: GameState): GameState {
        require(!state.finished) {
            "Cannot resolve night kills after game is finished."
        }

        var currentState = state

        val killedTargets = mutableListOf<Pair<Int, KillReason>>()

        val mafiaTargetId = state.pendingMafiaKillTargetId
        if (mafiaTargetId != null && mafiaTargetId != state.protectedPlayerId) {
            killedTargets.add(mafiaTargetId to KillReason.MAFIA_KILL)
        }

        val maniacTargetId = state.pendingManiacKillTargetId
        if (maniacTargetId != null && maniacTargetId != state.protectedPlayerId) {
            killedTargets.add(maniacTargetId to KillReason.MANIAC_KILL)
        }

        val distinctKills = killedTargets.distinctBy { it.first }

        val escort = currentState.aliveEscort()
        val escortWillBeKilled = escort != null &&
                distinctKills.any { it.first == escort.id }

        val escortVisitedPlayerId = currentState.escortVisitedPlayerId

        val finalKills = distinctKills.toMutableList()

        if (escortWillBeKilled && escortVisitedPlayerId != null) {
            val visitedPlayer = currentState.playerById(escortVisitedPlayerId)

            if (visitedPlayer.alive && visitedPlayer.id != currentState.protectedPlayerId) {
                finalKills.add(visitedPlayer.id to KillReason.ESCORT_LINK)
            }
        }

        for ((playerId, reason) in finalKills.distinctBy { it.first }) {
            if (currentState.playerById(playerId).alive) {
                DevLog.info("Night resolution kills player $playerId by $reason")

                currentState = GameEngine.killPlayer(
                    state = currentState,
                    playerId = playerId,
                    reason = reason
                )

                if (currentState.finished) {
                    return currentState
                }
            }
        }

        return currentState
    }
}