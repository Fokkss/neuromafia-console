package neuromafia.core.engine

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

object PhaseManager {
    fun startDayVoting(state: GameState): GameState {
        require(!state.finished) {
            "Cannot start day voting after game is finished."
        }

        require(state.phase == Phase.DAY_DISCUSSION) {
            "Day voting can be started only after day discussion."
        }

        return transitionTo(
            state = state,
            nextPhase = Phase.DAY_VOTING
        )
    }

    fun startNight(state: GameState): GameState {
        require(!state.finished) {
            "Cannot start night after game is finished."
        }

        require(state.phase == Phase.DAY_VOTING) {
            "Night can be started only after day voting."
        }

        return transitionTo(
            state = state,
            nextPhase = Phase.NIGHT_MAFIA
        )
    }

    fun finishCurrentNightPhase(state: GameState): GameState {
        require(!state.finished) {
            "Cannot finish night phase after game is finished."
        }

        return when (state.phase) {
            Phase.NIGHT_MAFIA -> transitionTo(
                state = state,
                nextPhase = Phase.NIGHT_GODFATHER
            )

            Phase.NIGHT_GODFATHER -> transitionTo(
                state = state,
                nextPhase = Phase.NIGHT_DOCTOR
            )

            Phase.NIGHT_DOCTOR -> transitionTo(
                state = state,
                nextPhase = Phase.NIGHT_COMMISSAR
            )

            Phase.NIGHT_COMMISSAR -> transitionTo(
                state = state,
                nextPhase = Phase.NIGHT_ESCORT
            )

            Phase.NIGHT_ESCORT -> transitionTo(
                state = state,
                nextPhase = Phase.NIGHT_MANIAC
            )

            Phase.NIGHT_MANIAC -> startNextDay(state)

            else -> error("Current phase ${state.phase} is not a night phase.")
        }
    }

    private fun startNextDay(state: GameState): GameState {
        val oldPhase = state.phase
        val newDayNumber = state.dayNumber + 1

        DevLog.info("Phase changed from $oldPhase to ${Phase.DAY_DISCUSSION}, day $newDayNumber")

        return state.copy(
            phase = Phase.DAY_DISCUSSION,
            dayNumber = newDayNumber,
            nominatedPlayerIds = emptyList(),
            eventLog = state.eventLog + GameEvent.PhaseChanged(
                from = oldPhase,
                to = Phase.DAY_DISCUSSION,
                dayNumber = newDayNumber
            )
        )
    }

    private fun transitionTo(
        state: GameState,
        nextPhase: Phase
    ): GameState {
        DevLog.info("Phase changed from ${state.phase} to $nextPhase, day ${state.dayNumber}")

        return state.copy(
            phase = nextPhase,
            eventLog = state.eventLog + GameEvent.PhaseChanged(
                from = state.phase,
                to = nextPhase,
                dayNumber = state.dayNumber
            )
        )
    }
}