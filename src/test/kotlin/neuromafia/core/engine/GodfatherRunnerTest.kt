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

class GodfatherRunnerTest {
    @Test
    fun `runGodfatherNight should resolve mafia kill tie`() {
        val state = testState(
            pendingMafiaKillCandidateIds = listOf(1, 2)
        )

        val runner = GodfatherRunner(
            controllersByPlayerId = mapOf(
                4 to mockGodfatherController(
                    godfatherId = 4,
                    selectedKillTargetId = 2,
                    checkedPlayerId = 1
                )
            )
        )

        val updatedState = runner.runGodfatherNight(state)

        assertEquals(2, updatedState.pendingMafiaKillTargetId)
        assertEquals(emptyList(), updatedState.pendingMafiaKillCandidateIds)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.GodfatherKillDecisionMade(
                    godfatherId = 4,
                    targetId = 2
                )
            )
        )
    }

    @Test
    fun `runGodfatherNight should check whether target is commissar`() {
        val state = testState()

        val runner = GodfatherRunner(
            controllersByPlayerId = mapOf(
                4 to mockGodfatherController(
                    godfatherId = 4,
                    selectedKillTargetId = 1,
                    checkedPlayerId = 2
                )
            )
        )

        val updatedState = runner.runGodfatherNight(state)

        assertEquals(true, updatedState.godfatherCommissarChecks[2])
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.GodfatherCommissarChecked(
                    godfatherId = 4,
                    targetId = 2,
                    isCommissar = true
                )
            )
        )
    }

    @Test
    fun `runGodfatherNight should store false when target is not commissar`() {
        val state = testState()

        val runner = GodfatherRunner(
            controllersByPlayerId = mapOf(
                4 to mockGodfatherController(
                    godfatherId = 4,
                    selectedKillTargetId = 1,
                    checkedPlayerId = 1
                )
            )
        )

        val updatedState = runner.runGodfatherNight(state)

        assertEquals(false, updatedState.godfatherCommissarChecks[1])
    }

    @Test
    fun `runGodfatherNight should fail outside godfather night phase`() {
        val state = testState(
            phase = Phase.NIGHT_MAFIA
        )

        val runner = GodfatherRunner(
            controllersByPlayerId = emptyMap()
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runGodfatherNight(state)
        }
    }

    @Test
    fun `runGodfatherNight should fail when godfather selects non candidate kill target`() {
        val state = testState(
            pendingMafiaKillCandidateIds = listOf(1, 2)
        )

        val runner = GodfatherRunner(
            controllersByPlayerId = mapOf(
                4 to mockGodfatherController(
                    godfatherId = 4,
                    selectedKillTargetId = 3,
                    checkedPlayerId = 1
                )
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runGodfatherNight(state)
        }
    }

    @Test
    fun `runGodfatherNight should fail when godfather checks himself`() {
        val state = testState()

        val runner = GodfatherRunner(
            controllersByPlayerId = mapOf(
                4 to mockGodfatherController(
                    godfatherId = 4,
                    selectedKillTargetId = 1,
                    checkedPlayerId = 4
                )
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runGodfatherNight(state)
        }
    }

    private fun mockGodfatherController(
        godfatherId: Int,
        selectedKillTargetId: Int,
        checkedPlayerId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseGodfatherKillDecision(any(), godfatherId, any())
        } returns PlayerAction.GodfatherKillDecision(
            godfatherId = godfatherId,
            targetId = selectedKillTargetId
        )

        every {
            controller.chooseGodfatherCommissarCheck(any(), godfatherId)
        } returns PlayerAction.GodfatherCommissarCheck(
            godfatherId = godfatherId,
            targetId = checkedPlayerId
        )

        return controller
    }

    private fun testState(
        phase: Phase = Phase.NIGHT_GODFATHER,
        pendingMafiaKillCandidateIds: List<Int> = emptyList()
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
            phase = phase,
            pendingMafiaKillCandidateIds = pendingMafiaKillCandidateIds
        )
    }
}