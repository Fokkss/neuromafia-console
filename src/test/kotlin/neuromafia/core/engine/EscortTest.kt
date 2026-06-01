package neuromafia.core.engine

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
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

class EscortMechanicsTest {
    @Test
    fun `escort visit should mute visited player on next day`() {
        val state = testState(
            phase = Phase.NIGHT_MANIAC,
            escortVisitedPlayerId = 1
        )

        val updatedState = PhaseManager.finishCurrentNightPhase(state)

        assertEquals(Phase.DAY_DISCUSSION, updatedState.phase)
        assertEquals(setOf(1), updatedState.mutedPlayerIds)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerMuted(
                    playerId = 1,
                    dayNumber = 2
                )
            )
        )
    }

    @Test
    fun `muted player should not speak during day discussion`() {
        val state = testState(
            phase = Phase.DAY_DISCUSSION,
            mutedPlayerIds = setOf(1)
        )

        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                2 to mockSpeechController(2),
                3 to mockSpeechController(3),
                4 to mockSpeechController(4),
                5 to mockSpeechController(5),
                6 to mockSpeechController(6),
                7 to mockSpeechController(7)
            )
        )

        val updatedState = runner.runDiscussion(state)

        val speechEvents = updatedState.eventLog.filterIsInstance<GameEvent.PlayerSpoke>()

        assertTrue(speechEvents.none { it.playerId == 1 })
    }

    @Test
    fun `muted player should not vote during day voting`() {
        val state = testState(
            phase = Phase.DAY_VOTING,
            mutedPlayerIds = setOf(1),
            nominatedPlayerIds = listOf(3, 4)
        )

        val runner = DayVotingRunner(
            controllersByPlayerId = mapOf(
                2 to mockVoteController(2, 4),
                3 to mockVoteController(3, 4),
                4 to mockVoteController(4, null),
                5 to mockVoteController(5, 4),
                6 to mockVoteController(6, 4),
                7 to mockVoteController(7, 4)
            )
        )

        val (updatedState, _) = runner.runVoting(state)

        val voteEvents = updatedState.eventLog.filterIsInstance<GameEvent.PlayerVoted>()

        assertTrue(voteEvents.none { it.voterId == 1 })
    }

    @Test
    fun `if escort is killed then visited player should also be killed`() {
        val state = testState(
            phase = Phase.NIGHT_MANIAC,
            escortVisitedPlayerId = 1,
            pendingMafiaKillTargetId = 6
        )

        val updatedState = NightResultsRunner.resolveNightKills(state)

        assertFalse(updatedState.playerById(6).alive)
        assertFalse(updatedState.playerById(1).alive)

        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerKilled(
                    playerId = 1,
                    reason = KillReason.ESCORT_LINK
                )
            )
        )
    }

    @Test
    fun `if visited player is killed but escort is alive then only visited player should die`() {
        val state = testState(
            phase = Phase.NIGHT_MANIAC,
            escortVisitedPlayerId = 1,
            pendingMafiaKillTargetId = 1
        )

        val updatedState = NightResultsRunner.resolveNightKills(state)

        assertFalse(updatedState.playerById(1).alive)
        assertTrue(updatedState.playerById(6).alive)
    }

    private fun mockSpeechController(
        playerId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseDaySpeech(any(), playerId)
        } returns PlayerAction.DaySpeech(
            playerId = playerId,
            message = "Text from player $playerId",
            nominatedPlayerId = null
        )

        return controller
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
        phase: Phase,
        escortVisitedPlayerId: Int? = null,
        mutedPlayerIds: Set<Int> = emptySet(),
        nominatedPlayerIds: List<Int> = emptyList(),
        pendingMafiaKillTargetId: Int? = null,
        pendingManiacKillTargetId: Int? = null
    ): GameState {
        val players = listOf(
            Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
            Player(id = 2, name = "Player 2", role = Role.COMMISSAR),
            Player(id = 3, name = "Player 3", role = Role.MAFIA),
            Player(id = 4, name = "Player 4", role = Role.GODFATHER),
            Player(id = 5, name = "Player 5", role = Role.DOCTOR),
            Player(id = 6, name = "Player 6", role = Role.ESCORT),
            Player(id = 7, name = "Player 7", role = Role.MANIAC)
        )

        return GameState(
            config = GameConfig(
                mode = GameMode.OBSERVE,
                playerCount = players.size,
                mafiaCount = 2,
                commissarEnabled = true,
                doctorEnabled = true,
                maniacEnabled = true,
                escortEnabled = true,
                provider = "stub",
                model = "stub",
                humanPlayerId = null
            ),
            players = players,
            phase = phase,
            escortVisitedPlayerId = escortVisitedPlayerId,
            mutedPlayerIds = mutedPlayerIds,
            nominatedPlayerIds = nominatedPlayerIds,
            pendingMafiaKillTargetId = pendingMafiaKillTargetId,
            pendingManiacKillTargetId = pendingManiacKillTargetId
        )
    }
}