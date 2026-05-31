package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class DayCycleRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runDay(state: GameState): GameState {
        require(!state.finished) {
            "Cannot run day after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Day cycle must start from DAY_DISCUSSION."
        }

        DevLog.info("Day cycle started, day ${state.dayNumber}")

        var currentState = state

        currentState = DayDiscussionRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runDiscussion(currentState)

        if (currentState.finished) {
            return currentState
        }

        currentState = PhaseManager.startDayVoting(currentState)

        val votingResult = DayVotingRunner(
            controllersByPlayerId = controllersByPlayerId
        ).runVoting(currentState)

        currentState = votingResult.first

        if (currentState.finished) {
            return currentState
        }

        currentState = PhaseManager.startNight(currentState)

        DevLog.info("Day cycle finished, night started")

        return currentState
    }
}