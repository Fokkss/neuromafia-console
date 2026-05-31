package neuromafia.core.engine

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import neuromafia.bot.PlayerController
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class NightMafiaRunnerTest {
    @Test
    fun `runMafiaKillVoting should select single leader as pending mafia kill target`() {
        val state = testState()

        val runner = NightMafiaRunner(
            controllersByPlayerId = mapOf(
                3 to mockMafiaKillVoteController(3, 1),
                4 to mockMafiaKillVoteController(4, 1)
            )
        )

        val updatedState = runner.runMafiaKillVoting(state)

        assertEquals(1, updatedState.pendingMafiaKillTargetId)
        assertEquals(emptyList(), updatedState.pendingMafiaKillCandidateIds)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.MafiaKillTargetSelected(
                    targetId = 1
                )
            )
        )
    }

    @Test
    fun `runMafiaKillVoting should store candidates when mafia voting is tied`() {
        val state = testState()

        val runner = NightMafiaRunner(
            controllersByPlayerId = mapOf(
                3 to mockMafiaKillVoteController(3, 1),
                4 to mockMafiaKillVoteController(4, 2)
            )
        )

        val updatedState = runner.runMafiaKillVoting(state)

        assertEquals(null, updatedState.pendingMafiaKillTargetId)
        assertEquals(listOf(1, 2), updatedState.pendingMafiaKillCandidateIds)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.MafiaKillTie(
                    candidateIds = listOf(1, 2)
                )
            )
        )
    }

    @Test
    fun `runMafiaKillVoting should record mafia votes`() {
        val state = testState()

        val runner = NightMafiaRunner(
            controllersByPlayerId = mapOf(
                3 to mockMafiaKillVoteController(3, 1),
                4 to mockMafiaKillVoteController(4, 2)
            )
        )

        val updatedState = runner.runMafiaKillVoting(state)

        val voteEvents = updatedState.eventLog.filterIsInstance<GameEvent.MafiaKillVoteRecorded>()

        assertEquals(2, voteEvents.size)
        assertTrue(
            voteEvents.contains(
                GameEvent.MafiaKillVoteRecorded(
                    mafiaId = 3,
                    targetId = 1
                )
            )
        )
    }

    @Test
    fun `runMafiaKillVoting should fail outside mafia night phase`() {
        val state = testState(
            phase = Phase.NIGHT_GODFATHER
        )

        val runner = NightMafiaRunner(
            controllersByPlayerId = emptyMap()
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runMafiaKillVoting(state)
        }
    }

    @Test
    fun `runMafiaKillVoting should fail when mafia votes for mafia teammate`() {
        val state = testState()

        val runner = NightMafiaRunner(
            controllersByPlayerId = mapOf(
                3 to mockMafiaKillVoteController(3, 4),
                4 to mockMafiaKillVoteController(4, 1)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runMafiaKillVoting(state)
        }
    }

    @Test
    fun `runMafiaKillVoting should fail when controller returns wrong mafia id`() {
        val state = testState()
        val controller = mockk<PlayerController>()

        every {
            controller.chooseMafiaKillVote(any(), 3)
        } returns PlayerAction.MafiaKillVote(
            mafiaId = 999,
            targetId = 1
        )

        val runner = NightMafiaRunner(
            controllersByPlayerId = mapOf(
                3 to controller,
                4 to mockMafiaKillVoteController(4, 1)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runMafiaKillVoting(state)
        }
    }

    private fun mockMafiaKillVoteController(
        mafiaId: Int,
        targetId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseMafiaKillVote(any(), mafiaId)
        } returns PlayerAction.MafiaKillVote(
            mafiaId = mafiaId,
            targetId = targetId
        )

        return controller
    }

    private fun testState(
        phase: Phase = Phase.NIGHT_MAFIA
    ): GameState {
        val players = listOf(
            Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
            Player(id = 2, name = "Player 2", role = Role.COMMISSAR),
            Player(id = 3, name = "Player 3", role = Role.MAFIA),
            Player(id = 4, name = "Player 4", role = Role.GODFATHER)
        )

        return GameState(
            config = GameConfig(
                mode = GameMode.OBSERVE,
                playerCount = players.size,
                mafiaCount = 2,
                commissarEnabled = true,
                doctorEnabled = false,
                maniacEnabled = false,
                escortEnabled = false,
                provider = "stub",
                model = "stub",
                humanPlayerId = null
            ),
            players = players,
            phase = phase
        )
    }
}