package neuromafia.core.engine

import kotlin.random.Random
import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Role
import neuromafia.dev.DevLog

class GodfatherRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val random: Random = Random.Default
) {
    fun runGodfatherNight(state: GameState): GameState {
        require(!state.finished) {
            "cannot run godfather night after game is finished."
        }

        require(state.phase == Phase.NIGHT_GODFATHER) {
            "godfather night can be run only during NIGHT_GODFATHER phase."
        }

        var currentState = state
        val godfather = currentState.aliveGodfather()

        currentState = resolveMafiaKillTieIfNeeded(
            state = currentState,
            godfatherId = godfather?.id
        )

        if (godfather == null) {
            DevLog.info("no alive godfather, commissar check skipped")
            return currentState
        }

        val controller = controllersByPlayerId[godfather.id]
            ?: error("no controller for godfather ${godfather.id}")

        val action = controller.chooseGodfatherCommissarCheck(
            state = currentState,
            playerId = godfather.id
        )

        require(action.godfatherId == godfather.id) {
            "controller for godfather ${godfather.id} returned check for player ${action.godfatherId}."
        }

        require(action.targetId != godfather.id) {
            "godfather cannot check himself."
        }

        val target = currentState.playerById(action.targetId)

        require(target.alive) {
            "godfather cannot check killed player ${target.id}."
        }

        val isCommissar = target.role == Role.COMMISSAR

        DevLog.info("godfather ${godfather.id} checked player ${target.id}, isCommissar=$isCommissar")

        return currentState.copy(
            godfatherCommissarChecks = currentState.godfatherCommissarChecks + (target.id to isCommissar),
            eventLog = currentState.eventLog + GameEvent.GodfatherCommissarChecked(
                godfatherId = godfather.id,
                targetId = target.id,
                isCommissar = isCommissar
            )
        )
    }

    private fun resolveMafiaKillTieIfNeeded(
        state: GameState,
        godfatherId: Int?
    ): GameState {
        if (state.pendingMafiaKillCandidateIds.isEmpty()) {
            return state
        }

        if (godfatherId == null) {
            val selectedTargetId = state.pendingMafiaKillCandidateIds.random(random)

            DevLog.info("no alive godfather, random mafia kill target selected: $selectedTargetId")

            return state.copy(
                pendingMafiaKillTargetId = selectedTargetId,
                pendingMafiaKillCandidateIds = emptyList(),
                eventLog = state.eventLog + GameEvent.MafiaKillTargetSelected(
                    targetId = selectedTargetId
                )
            )
        }

        val controller = controllersByPlayerId[godfatherId]
            ?: error("no controller for godfather $godfatherId")

        val action = controller.chooseGodfatherKillDecision(
            state = state,
            playerId = godfatherId,
            candidateIds = state.pendingMafiaKillCandidateIds
        )

        require(action.godfatherId == godfatherId) {
            "controller for godfather $godfatherId returned kill decision for player ${action.godfatherId}."
        }

        require(action.targetId in state.pendingMafiaKillCandidateIds) {
            "godfather selected player ${action.targetId}, but candidates are ${state.pendingMafiaKillCandidateIds}."
        }

        DevLog.info("godfather $godfatherId selected final mafia kill target ${action.targetId}")

        return state.copy(
            pendingMafiaKillTargetId = action.targetId,
            pendingMafiaKillCandidateIds = emptyList(),
            eventLog = state.eventLog + GameEvent.GodfatherKillDecisionMade(
                godfatherId = godfatherId,
                targetId = action.targetId
            )
        )
    }
}