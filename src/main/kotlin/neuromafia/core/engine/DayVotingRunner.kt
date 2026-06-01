package neuromafia.core.engine

import neuromafia.bot.PlayerController
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Phase
import neuromafia.dev.DevLog

class DayVotingRunner(
    private val controllersByPlayerId: Map<Int, PlayerController>,
    private val onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
) {
    fun runVoting(state: GameState): Pair<GameState, DayVoting> {
        require(!state.finished) {
            "cannot run day voting after game is finished."
        }

        require(state.phase == Phase.DAY_VOTING) {
            "day voting can be run only during DAY_VOTING phase."
        }

        if (state.nominatedPlayerIds.isEmpty()) {
            DevLog.info("day voting skipped: no nominated players")
            return state to DayVoting.NoCandidates
        }

        val nominatedPlayers = state.nominatedPlayerIds.map { nominatedPlayerId ->
            state.playerById(nominatedPlayerId)
        }

        require(nominatedPlayers.all { it.alive }) {
            "all nominated players must be alive."
        }

        var currentState = state
        val votesByTargetId = mutableMapOf<Int, Int>()

        val voters = currentState.alivePlayers()
            .filter { it.id !in currentState.mutedPlayerIds }
            .sortedBy { it.id }

        DevLog.info("running day voting with candidates ${state.nominatedPlayerIds}")

        for (voter in voters) {
            val controller = controllersByPlayerId[voter.id]
                ?: error("no controller for player ${voter.id}")

            val action = controller.chooseDayVote(
                state = currentState,
                playerId = voter.id,
                nominatedPlayerIds = currentState.nominatedPlayerIds
            )

            require(action.voterId == voter.id) {
                "controller for player ${voter.id} returned vote for player ${action.voterId}."
            }

            if (action.targetId != null) {
                require(action.targetId in currentState.nominatedPlayerIds) {
                    "player ${voter.id} voted for non-nominated player ${action.targetId}."
                }

                require(currentState.playerById(action.targetId).alive) {
                    "player ${voter.id} voted for killed player ${action.targetId}."
                }

                votesByTargetId[action.targetId] = votesByTargetId.getOrDefault(action.targetId, 0) + 1
            }

            val previousState = currentState

            currentState = currentState.copy(
                eventLog = currentState.eventLog + GameEvent.PlayerVoted(
                    voterId = action.voterId,
                    targetId = action.targetId
                )
            )

            onStateChanged(previousState, currentState)
        }

        if (votesByTargetId.isEmpty()) {
            DevLog.info("day voting finished: nobody voted")
            return currentState to DayVoting.Tie(
                candidateIds = currentState.nominatedPlayerIds
            )
        }

        val maxVotes = votesByTargetId.values.max()
        val leaders = votesByTargetId
            .filterValues { voteCount -> voteCount == maxVotes }
            .keys
            .sorted()

        if (leaders.size != 1) {
            DevLog.info("day voting tie between $leaders")

            val previousState = currentState

            val tiedState = currentState.copy(
                eventLog = currentState.eventLog + GameEvent.DayVotingTie(
                    candidateIds = leaders
                )
            )

            onStateChanged(previousState, tiedState)

            return tiedState to DayVoting.Tie(
                candidateIds = leaders
            )
        }

        val killedPlayerId = leaders.single()

        DevLog.info("day voting killed player $killedPlayerId")

        val previousState = currentState

        val updatedState = GameEngine.killPlayer(
            state = currentState,
            playerId = killedPlayerId,
            reason = KillReason.DAY_VOTE
        )

        onStateChanged(previousState, updatedState)

        return updatedState to DayVoting.Killed(
            playerId = killedPlayerId
        )
    }
}