package neuromafia.core.engine

import neuromafia.core.model.KillReason
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.dev.DevLog

object GameEngine {
    fun killPlayer(
        state: GameState,
        playerId: Int,
        reason: KillReason
    ): GameState {
        require(!state.finished) {
            "Cannot kill player after game is finished."
        }

        val target = state.playerById(playerId)

        require(target.alive) {
            "Player $playerId is already killed."
        }

        DevLog.info("Killing player $playerId by reason $reason")

        val updatedPlayers = state.players.map { player ->
            if (player.id == playerId) {
                player.copy(alive = false)
            } else {
                player
            }
        }

        val updatedState = state.copy(
            players = updatedPlayers,
            eventLog = state.eventLog + GameEvent.PlayerKilled(
                playerId = playerId,
                reason = reason
            )
        )

        return WinConditionChecker.updateState(updatedState)
    }
}