package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class EscortRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runEscortNight(state: GameState): GameState {
        require(!state.finished) {
            "cannot run escort night after game is finished."
        }

        require(state.phase == Phase.NIGHT_ESCORT) {
            "escort night can be run only during NIGHT_ESCORT phase."
        }

        val escort = state.aliveEscort()

        if (escort == null) {
            DevLog.info("no alive escort, escort night skipped")
            return state.copy(escortVisitedPlayerId = null)
        }

        val controller = controllersByPlayerId[escort.id]
            ?: error("no controller for escort ${escort.id}")

        val action = controller.chooseEscortVisit(
            state = state,
            playerId = escort.id
        )

        require(action.escortId == escort.id) {
            "controller for escort ${escort.id} returned visit for player ${action.escortId}."
        }

        require(action.targetId != escort.id) {
            "escort cannot visit herself."
        }

        val target = state.playerById(action.targetId)

        require(target.alive) {
            "escort cannot visit killed player ${target.id}."
        }

        DevLog.info("escort ${escort.id} visited player ${target.id}")

        return state.copy(
            escortVisitedPlayerId = target.id,
            eventLog = state.eventLog + GameEvent.EscortVisited(
                escortId = escort.id,
                targetId = target.id
            )
        )
    }
}