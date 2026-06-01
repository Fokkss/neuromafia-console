package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class DoctorRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runDoctorNight(state: GameState): GameState {
        require(!state.finished) {
            "cannot run doctor night after game is finished."
        }

        require(state.phase == Phase.NIGHT_DOCTOR) {
            "doctor night can be run only during NIGHT_DOCTOR phase."
        }

        val doctor = state.aliveDoctor()

        if (doctor == null) {
            DevLog.info("no alive doctor, doctor night skipped")
            return state.copy(protectedPlayerId = null)
        }

        val controller = controllersByPlayerId[doctor.id]
            ?: error("no controller for doctor ${doctor.id}")

        val action = controller.chooseDoctorHeal(
            state = state,
            playerId = doctor.id
        )

        require(action.doctorId == doctor.id) {
            "controller for doctor ${doctor.id} returned heal for player ${action.doctorId}."
        }

        val target = state.playerById(action.targetId)

        require(target.alive) {
            "doctor cannot protect killed player ${target.id}."
        }

        DevLog.info("doctor ${doctor.id} protected player ${target.id}")

        return state.copy(
            protectedPlayerId = target.id,
            eventLog = state.eventLog + GameEvent.DoctorProtected(
                doctorId = doctor.id,
                targetId = target.id
            )
        )
    }
}