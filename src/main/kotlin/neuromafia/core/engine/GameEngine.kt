package neuromafia.core.engine

import neuromafia.core.model.KillReason
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

object GameEngine {
    fun recordDaySpeech(
        state: GameState,
        playerId: Int,
        message: String
    ): GameState {
        require(!state.finished) {
            "cannot record speech after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "player can speak only during day discussion."
        }

        val player = state.playerById(playerId)

        require(player.alive) {
            "killed player $playerId cannot speak."
        }

        DevLog.info("recorded day speech from player $playerId")

        return state.copy(
            eventLog = state.eventLog + GameEvent.PlayerSpoke(
                playerId = playerId,
                message = message
            )
        )
    }

    fun killPlayer(
        state: GameState,
        playerId: Int,
        reason: KillReason
    ): GameState {
        require(!state.finished) {
            "cannot kill player after game is finished."
        }

        val target = state.playerById(playerId)

        require(target.alive) {
            "player $playerId is already killed."
        }

        DevLog.info("killing player $playerId by reason $reason")

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
            "cannot nominate player after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "players can be nominated only during day discussion."
        }

        val speaker = state.playerById(speakerId)
        val nominatedPlayer = state.playerById(nominatedPlayerId)

        require(speaker.alive) {
            "speaker $speakerId is killed."
        }

        require(nominatedPlayer.alive) {
            "nominated player $nominatedPlayerId is killed."
        }

        require(speakerId != nominatedPlayerId) {
            "player cannot nominate himself."
        }

        require(nominatedPlayerId !in state.nominatedPlayerIds) {
            "player $nominatedPlayerId is already nominated."
        }

        DevLog.info("player $speakerId nominated player $nominatedPlayerId")

        return state.copy(
            nominatedPlayerIds = state.nominatedPlayerIds + nominatedPlayerId,
            eventLog = state.eventLog + GameEvent.PlayerNominated(
                speakerId = speakerId,
                nominatedPlayerId = nominatedPlayerId
            )
        )
    }
}