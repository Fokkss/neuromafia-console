package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Team
import neuromafia.dev.DevLog

class CommissarRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runCommissarNight(state: GameState): GameState {
        require(!state.finished) {
            "cannot run commissar night after game is finished."
        }

        require(state.phase == Phase.NIGHT_COMMISSAR) {
            "commissar night can be run only during NIGHT_COMMISSAR phase."
        }

        val commissar = state.aliveCommissar()

        if (commissar == null) {
            DevLog.info("no alive commissar, commissar night skipped")
            return state
        }

        val controller = controllersByPlayerId[commissar.id]
            ?: error("no controller for commissar ${commissar.id}")

        val action = controller.chooseCommissarCheck(
            state = state,
            playerId = commissar.id
        )

        require(action.commissarId == commissar.id) {
            "controller for commissar ${commissar.id} returned check for player ${action.commissarId}."
        }

        require(action.targetId != commissar.id) {
            "commissar cannot check himself."
        }

        val target = state.playerById(action.targetId)

        require(target.alive) {
            "commissar cannot check killed player ${target.id}."
        }

        val isMafia = target.role.team == Team.MAFIA

        DevLog.info("commissar ${commissar.id} checked player ${target.id}, isMafia=$isMafia")

        return state.copy(
            commissarChecks = state.commissarChecks + (target.id to isMafia),
            eventLog = state.eventLog + GameEvent.CommissarChecked(
                commissarId = commissar.id,
                targetId = target.id,
                isMafia = isMafia
            )
        )
    }
}