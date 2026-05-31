package neuromafia.core.engine

import neuromafia.core.model.KillReason
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
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

    fun nominatePlayer(
        state: GameState,
        speakerId: Int,
        nominatedPlayerId: Int
    ): GameState {
        require(!state.finished) {
            "Cannot nominate player after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Players can be nominated only during day discussion."
        }

        val speaker = state.playerById(speakerId)
        val nominatedPlayer = state.playerById(nominatedPlayerId)

        require(speaker.alive) {
            "Speaker $speakerId is killed."
        }

        require(nominatedPlayer.alive) {
            "Nominated player $nominatedPlayerId is killed."
        }

        require(speakerId != nominatedPlayerId) {
            "Player cannot nominate himself."
        }

        require(nominatedPlayerId !in state.nominatedPlayerIds) {
            "Player $nominatedPlayerId is already nominated."
        }

        DevLog.info("Player $speakerId nominated player $nominatedPlayerId")

        return state.copy(
            nominatedPlayerIds = state.nominatedPlayerIds + nominatedPlayerId,
            eventLog = state.eventLog + GameEvent.PlayerNominated(
                speakerId = speakerId,
                nominatedPlayerId = nominatedPlayerId
            )
        )
    }
}