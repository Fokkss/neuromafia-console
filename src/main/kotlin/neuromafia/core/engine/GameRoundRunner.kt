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
            "cannot run round after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "game round must start from DAY_DISCUSSION."
        }

        DevLog.info("game round started, day ${state.dayNumber}")

        var currentState = DayCycleRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runDay(state)

        if (currentState.finished) {
            return currentState
        }

        currentState = NightCycleRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runNight(currentState)

        DevLog.info("game round finished")

        return currentState
    }
}