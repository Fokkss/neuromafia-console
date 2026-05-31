package neuromafia.core.engine

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import neuromafia.bot.PlayerController
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class DayVotingRunnerTest {
    @Test
    fun `runVoting should kill candidate with most votes`() {
        val state = testState(
            nominatedPlayerIds = listOf(3, 4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to mockVoteController(1, 4),
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, 3),
                4 to mockVoteController(4, null)
            )
        )

        val (updatedState, result) = runner.runVoting(state)

        assertEquals(DayVoting.Killed(4), result)
        assertFalse(updatedState.playerById(4).alive)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerKilled(
                    playerId = 4,
                    reason = KillReason.DAY_VOTE
                )
            )
        )
    }

    @Test
    fun `runVoting should record votes`() {
        val state = testState(
            nominatedPlayerIds = listOf(3, 4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to mockVoteController(1, 4),
                2 to mockVoteController(2, 3),
                3 to mockVoteController(3, null),
                4 to mockVoteController(4, null)
            )
        )

        val (updatedState, result) = runner.runVoting(state)

        assertEquals(DayVoting.Tie(listOf(3, 4)), result)

        val voteEvents = updatedState.eventLog.filterIsInstance<GameEvent.PlayerVoted>()

        assertEquals(4, voteEvents.size)
        assertTrue(
            voteEvents.contains(
                GameEvent.PlayerVoted(
                    voterId = 1,
                    targetId = 4
                )
            )
        )
    }

    @Test
    fun `runVoting should return tie when candidates have equal votes`() {
        val state = testState(
            nominatedPlayerIds = listOf(3, 4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to mockVoteController(1, 3),
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, null),
                4 to mockVoteController(4, null)
            )
        )

        val (updatedState, result) = runner.runVoting(state)

        assertEquals(DayVoting.Tie(listOf(3, 4)), result)
        assertTrue(updatedState.playerById(3).alive)
        assertTrue(updatedState.playerById(4).alive)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.DayVotingTie(
                    candidateIds = listOf(3, 4)
                )
            )
        )
    }

    @Test
    fun `runVoting should return no candidates when nobody is nominated`() {
        val state = testState(
            nominatedPlayerIds = emptyList()
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = emptyMap()
        )

        val (updatedState, result) = runner.runVoting(state)

        assertEquals(DayVoting.NoCandidates, result)
        assertEquals(state, updatedState)
    }

    @Test
    fun `runVoting should fail outside voting phase`() {
        val state = testState(
            phase = Phase.DAY_DISCUSSION,
            nominatedPlayerIds = listOf(4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to mockVoteController(1, 4),
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, 4),
                4 to mockVoteController(4, null)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runVoting(state)
        }
    }

    @Test
    fun `runVoting should fail when controller is missing`() {
        val state = testState(
            nominatedPlayerIds = listOf(4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to mockVoteController(1, 4),
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, 4)
            )
        )

        assertFailsWith<IllegalStateException> {
            runner.runVoting(state)
        }
    }

    @Test
    fun `runVoting should fail when vote target is not nominated`() {
        val state = testState(
            nominatedPlayerIds = listOf(4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to mockVoteController(1, 3),
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, 4),
                4 to mockVoteController(4, null)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runVoting(state)
        }
    }

    @Test
    fun `runVoting should fail when controller returns wrong voter id`() {
        val state = testState(
            nominatedPlayerIds = listOf(4)
        )

        val controller = mockk<PlayerController>()

        every {
            controller.chooseDayVote(any(), 1, listOf(4))
        } returns PlayerAction.DayVote(
            voterId = 999,
            targetId = 4
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                1 to controller,
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, 4),
                4 to mockVoteController(4, null)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runVoting(state)
        }
    }

    private fun mockVoteController(
        voterId: Int,
        targetId: Int?
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseDayVote(any(), voterId, any())
        } returns PlayerAction.DayVote(
            voterId = voterId,
            targetId = targetId
        )

        return controller
    }

    private fun testState(
        phase: Phase = Phase.DAY_VOTING,
        nominatedPlayerIds: List<Int> = listOf(4),
        players: List<Player> = defaultPlayers()
    ): GameState {
        return GameState(
            config = GameConfig(
                mode = GameMode.OBSERVE,
                playerCount = players.size,
                mafiaCount = players.count { it.role == Role.MAFIA || it.role == Role.GODFATHER },
                commissarEnabled = players.any { it.role == Role.COMMISSAR },
                doctorEnabled = players.any { it.role == Role.DOCTOR },
                maniacEnabled = players.any { it.role == Role.MANIAC },
                escortEnabled = players.any { it.role == Role.ESCORT },
                provider = "stub",
                model = "stub",
                humanPlayerId = null
            ),
            players = players,
            phase = phase,
            nominatedPlayerIds = nominatedPlayerIds
        )
    }

    private fun defaultPlayers(): List<Player> {
        return listOf(
            Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
            Player(id = 2, name = "Player 2", role = Role.CIVILIAN),
            Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
            Player(id = 4, name = "Player 4", role = Role.GODFATHER)
        )
    }
}