package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class DayCycleRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
) {
    fun runDay(state: GameState): GameState {
        require(!state.finished) {
            "Cannot run day after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Day cycle must start from DAY_DISCUSSION."
        }

        DevLog.info("Day cycle started, day ${state.dayNumber}")

        var currentState = DayDiscussionRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runDiscussion(state)

        if (currentState.finished) {
            return currentState
        }

        var previousState = currentState
        currentState = PhaseManager.startDayVoting(currentState)
        onStateChanged(previousState, currentState)

        previousState = currentState
        val votingResult = DayVotingRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runVoting(currentState)
        currentState = votingResult.first

        if (currentState.finished) {
            return currentState
        }

        previousState = currentState
        currentState = PhaseManager.startNight(currentState)
        onStateChanged(previousState, currentState)

        DevLog.info("Day cycle finished, night started")

        return currentState
    }
}