package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class GameRoundRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
) {
    fun runRound(state: GameState): GameState {
        require(!state.finished) {
            "Cannot run round after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Game round must start from DAY_DISCUSSION."
        }

        DevLog.info("Game round started, day ${state.dayNumber}")

        var currentState = state

        val beforeDay = currentState

        currentState = DayCycleRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runDay(currentState)

        onStateChanged(beforeDay, currentState)

        if (currentState.finished) {
            return currentState
        }

        val beforeNight = currentState

        currentState = NightCycleRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runNight(currentState)

        onStateChanged(beforeNight, currentState)

        DevLog.info("Game round finished")

        return currentState
    }
}