package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.dev.DevLog

class GameLoopRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
) {
    fun runUntilFinished(
        initialState: GameState,
        maxRounds: Int = 50
    ): GameState {
        require(maxRounds > 0) {
            "Max rounds must be positive."
        }

        var currentState = initialState
        var round = 0

        while (!currentState.finished && round < maxRounds) {
            round += 1

            DevLog.info("Running round $round")

            currentState = GameRoundRunner(
                controllersByPlayerId = controllersByPlayerId,
                onStateChanged = onStateChanged
            ).runRound(currentState)
        }

        return currentState
    }
}