package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class GameRoundRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runRound(state: GameState): GameState {
        require(!state.finished) {
            "Cannot run round after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Game round must start from DAY_DISCUSSION."
        }

        DevLog.info("Game round started, day ${state.dayNumber}")

        var currentState = DayCycleRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runDay(state)

        if (currentState.finished) {
            return currentState
        }

        currentState = NightCycleRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runNight(currentState)

        DevLog.info("Game round finished")

        return currentState
    }
}