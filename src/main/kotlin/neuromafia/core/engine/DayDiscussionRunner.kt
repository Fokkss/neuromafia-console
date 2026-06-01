package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class DayDiscussionRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
) {
    fun runDiscussion(state: GameState): GameState {
        require(!state.finished) {
            "cannot run day discussion after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "day discussion can be run only during DAY_DISCUSSION phase."
        }

        var currentState = state

        val speakers = currentState.alivePlayers()
            .filter { it.id !in currentState.mutedPlayerIds }
            .sortedBy { it.id }

        DevLog.info("running day discussion for ${speakers.size} alive players")

        for (speaker in speakers) {
            val controller = controllersByPlayerId[speaker.id]
                ?: error("no controller for player ${speaker.id}")

            val action = controller.chooseDaySpeech(
                state = currentState,
                playerId = speaker.id
            )

            require(action.playerId == speaker.id) {
                "controller for player ${speaker.id} returned action for player ${action.playerId}."
            }

            var previousState = currentState

            currentState = GameEngine.recordDaySpeech(
                state = currentState,
                playerId = action.playerId,
                message = action.message
            )

            onStateChanged(previousState, currentState)

            if (action.nominatedPlayerId != null) {
                previousState = currentState

                currentState = try {
                    GameEngine.nominatePlayer(
                        state = currentState,
                        speakerId = action.playerId,
                        nominatedPlayerId = action.nominatedPlayerId
                    )
                } catch (exception: IllegalArgumentException) {
                    DevLog.info(
                        "invalid nomination from player ${action.playerId}: " +
                                "${action.nominatedPlayerId}, reason: ${exception.message}"
                    )

                    currentState
                }

                onStateChanged(previousState, currentState)
            }
        }

        return currentState
    }
}