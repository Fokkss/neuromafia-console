package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Team
import neuromafia.dev.DevLog

class NightMafiaRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>
) {
    fun runMafiaKillVoting(state: GameState): GameState {
        require(!state.finished) {
            "cannot run mafia night after game is finished."
        }

        require(state.phase == Phase.NIGHT_MAFIA) {
            "mafia kill voting can be run only during NIGHT_MAFIA phase."
        }

        val mafiaPlayers = state.aliveMafiaPlayers()
            .sortedBy { it.id }

        if (mafiaPlayers.isEmpty()) {
            DevLog.info("no alive mafia players, mafia night skipped")
            return state
        }

        var currentState = state.copy(
            pendingMafiaKillTargetId = null,
            pendingMafiaKillCandidateIds = emptyList()
        )

        val votesByTargetId = mutableMapOf<Int, Int>()

        for (mafiaPlayer in mafiaPlayers) {
            val controller = controllersByPlayerId[mafiaPlayer.id]
                ?: error("no controller for mafia player ${mafiaPlayer.id}")

            val action = controller.chooseMafiaKillVote(
                state = currentState,
                playerId = mafiaPlayer.id
            )

            require(action.mafiaId == mafiaPlayer.id) {
                "controller for player ${mafiaPlayer.id} returned mafia vote for player ${action.mafiaId}."
            }

            val target = currentState.playerById(action.targetId)

            require(target.alive) {
                "mafia player ${mafiaPlayer.id} voted for killed player ${target.id}."
            }

            require(target.role.team != Team.MAFIA) {
                "mafia player ${mafiaPlayer.id} cannot vote to kill mafia teammate ${target.id}."
            }

            votesByTargetId[action.targetId] = votesByTargetId.getOrDefault(action.targetId, 0) + 1

            currentState = currentState.copy(
                eventLog = currentState.eventLog + GameEvent.MafiaKillVoteRecorded(
                    mafiaId = action.mafiaId,
                    targetId = action.targetId
                )
            )
        }

        val maxVotes = votesByTargetId.values.max()
        val leaders = votesByTargetId
            .filterValues { voteCount -> voteCount == maxVotes }
            .keys
            .sorted()

        if (leaders.size == 1) {
            val targetId = leaders.single()

            DevLog.info("mafia selected kill target $targetId")

            return currentState.copy(
                pendingMafiaKillTargetId = targetId,
                pendingMafiaKillCandidateIds = emptyList(),
                eventLog = currentState.eventLog + GameEvent.MafiaKillTargetSelected(
                    targetId = targetId
                )
            )
        }

        DevLog.info("mafia kill voting tie between $leaders")

        return currentState.copy(
            pendingMafiaKillTargetId = null,
            pendingMafiaKillCandidateIds = leaders,
            eventLog = currentState.eventLog + GameEvent.MafiaKillTie(
                candidateIds = leaders
            )
        )
    }
}