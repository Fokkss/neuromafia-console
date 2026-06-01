package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class ManiacRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runManiacNight(state: GameState): GameState {
        require(!state.finished) {
            "cannot run maniac night after game is finished."
        }

        require(state.phase == Phase.NIGHT_MANIAC) {
            "maniac night can be run only during NIGHT_MANIAC phase."
        }

        val maniac = state.aliveManiac()

        if (maniac == null) {
            DevLog.info("no alive maniac, maniac night skipped")
            return state.copy(pendingManiacKillTargetId = null)
        }

        val controller = controllersByPlayerId[maniac.id]
            ?: error("no controller for maniac ${maniac.id}")

        val action = controller.chooseManiacKill(
            state = state,
            playerId = maniac.id
        )

        require(action.maniacId == maniac.id) {
            "controller for maniac ${maniac.id} returned kill for player ${action.maniacId}."
        }

        require(action.targetId != maniac.id) {
            "maniac cannot kill himself."
        }

        val target = state.playerById(action.targetId)

        require(target.alive) {
            "maniac cannot kill killed player ${target.id}."
        }

        DevLog.info("maniac ${maniac.id} selected kill target ${target.id}")

        return state.copy(
            pendingManiacKillTargetId = target.id,
            eventLog = state.eventLog + GameEvent.ManiacKillTargetSelected(
                maniacId = maniac.id,
                targetId = target.id
            )
        )
    }
}