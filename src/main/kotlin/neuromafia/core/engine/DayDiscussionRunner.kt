package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class DayDiscussionRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runDiscussion(state: GameState): GameState {
        require(!state.finished) {
            "Cannot run day discussion after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Day discussion can be run only during DAY_DISCUSSION phase."
        }

        var currentState = state

        val speakers = currentState.alivePlayers()
            .filter { it.id !in currentState.mutedPlayerIds }
            .sortedBy { it.id }

        DevLog.info("Running day discussion for ${speakers.size} alive players")

        for (speaker in speakers) {
            val controller = controllersByPlayerId[speaker.id]
                ?: error("No controller for player ${speaker.id}")

            val action = controller.chooseDaySpeech(
                state = currentState,
                playerId = speaker.id
            )

            require(action.playerId == speaker.id) {
                "Controller for player ${speaker.id} returned action for player ${action.playerId}."
            }

            currentState = GameEngine.recordDaySpeech(
                state = currentState,
                playerId = action.playerId,
                message = action.message
            )

            if (action.nominatedPlayerId != null) {
                currentState = try {
                    GameEngine.nominatePlayer(
                        state = currentState,
                        speakerId = action.playerId,
                        nominatedPlayerId = action.nominatedPlayerId
                    )
                } catch (exception: IllegalArgumentException) {
                    DevLog.info(
                        "Invalid nomination from player ${action.playerId}: " +
                                "${action.nominatedPlayerId}. Reason: ${exception.message}"
                    )

                    currentState
                }
            }
        }

        return currentState
    }
}