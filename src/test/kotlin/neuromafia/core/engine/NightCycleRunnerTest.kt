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

class NightCycleRunnerTest {
    @Test
    fun `runNight should run all night phases and start next day`() {
        val state = testState()

        val runner = NightCycleRunner(
            controllersByPlayerId = mapOf(
                2 to commissarController(
                    commissarId = 2,
                    targetId = 4
                ),
                3 to mafiaController(
                    mafiaId = 3,
                    targetId = 1
                ),
                4 to godfatherController(
                    godfatherId = 4,
                    checkedPlayerId = 2,
                    selectedKillTargetId = 1
                ),
                5 to doctorController(
                    doctorId = 5,
                    targetId = 8
                ),
                6 to escortController(
                    escortId = 6,
                    targetId = 8
                ),
                7 to maniacController(
                    maniacId = 7,
                    targetId = 2
                )
            )
        )

        val updatedState = runner.runNight(state)

        assertEquals(Phase.DAY_DISCUSSION, updatedState.phase)
        assertEquals(2, updatedState.dayNumber)

        assertFalse(updatedState.playerById(1).alive)
        assertFalse(updatedState.playerById(2).alive)

        assertEquals(setOf(8), updatedState.mutedPlayerIds)
        assertEquals(null, updatedState.pendingMafiaKillTargetId)
        assertEquals(null, updatedState.pendingManiacKillTargetId)
        assertEquals(null, updatedState.protectedPlayerId)
        assertEquals(null, updatedState.escortVisitedPlayerId)
    }

    @Test
    fun `runNight should keep mafia target alive when doctor protects target`() {
        val state = testState()

        val runner = NightCycleRunner(
            controllersByPlayerId = mapOf(
                2 to commissarController(
                    commissarId = 2,
                    targetId = 4
                ),
                3 to mafiaController(
                    mafiaId = 3,
                    targetId = 1
                ),
                4 to godfatherController(
                    godfatherId = 4,
                    checkedPlayerId = 2,
                    selectedKillTargetId = 1
                ),
                5 to doctorController(
                    doctorId = 5,
                    targetId = 1
                ),
                6 to escortController(
                    escortId = 6,
                    targetId = 8
                ),
                7 to maniacController(
                    maniacId = 7,
                    targetId = 8
                )
            )
        )

        val updatedState = runner.runNight(state)

        assertTrue(updatedState.playerById(1).alive)
        assertFalse(updatedState.playerById(8).alive)
        assertEquals(emptySet(), updatedState.mutedPlayerIds)
    }

    @Test
    fun `runNight should kill visited player when escort is killed`() {
        val state = testState()

        val runner = NightCycleRunner(
            controllersByPlayerId = mapOf(
                2 to commissarController(
                    commissarId = 2,
                    targetId = 4
                ),
                3 to mafiaController(
                    mafiaId = 3,
                    targetId = 6
                ),
                4 to godfatherController(
                    godfatherId = 4,
                    checkedPlayerId = 2,
                    selectedKillTargetId = 6
                ),
                5 to doctorController(
                    doctorId = 5,
                    targetId = 1
                ),
                6 to escortController(
                    escortId = 6,
                    targetId = 8
                ),
                7 to maniacController(
                    maniacId = 7,
                    targetId = 1
                )
            )
        )

        val updatedState = runner.runNight(state)

        assertFalse(updatedState.playerById(6).alive)
        assertFalse(updatedState.playerById(8).alive)

        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerKilled(
                    playerId = 8,
                    reason = KillReason.ESCORT_LINK
                )
            )
        )
    }

    private fun mafiaController(
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

    private fun godfatherController(
        godfatherId: Int,
        checkedPlayerId: Int,
        selectedKillTargetId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseMafiaKillVote(any(), godfatherId)
        } returns PlayerAction.MafiaKillVote(
            mafiaId = godfatherId,
            targetId = selectedKillTargetId
        )

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

    private fun doctorController(
        doctorId: Int,
        targetId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseDoctorHeal(any(), doctorId)
        } returns PlayerAction.DoctorHeal(
            doctorId = doctorId,
            targetId = targetId
        )

        return controller
    }

    private fun commissarController(
        commissarId: Int,
        targetId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseCommissarCheck(any(), commissarId)
        } returns PlayerAction.CommissarCheck(
            commissarId = commissarId,
            targetId = targetId
        )

        return controller
    }

    private fun escortController(
        escortId: Int,
        targetId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseEscortVisit(any(), escortId)
        } returns PlayerAction.EscortVisit(
            escortId = escortId,
            targetId = targetId
        )

        return controller
    }

    private fun maniacController(
        maniacId: Int,
        targetId: Int
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseManiacKill(any(), maniacId)
        } returns PlayerAction.ManiacKill(
            maniacId = maniacId,
            targetId = targetId
        )

        return controller
    }

    private fun testState(): GameState {
        val players = listOf(
            Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
            Player(id = 2, name = "Player 2", role = Role.COMMISSAR),
            Player(id = 3, name = "Player 3", role = Role.MAFIA),
            Player(id = 4, name = "Player 4", role = Role.GODFATHER),
            Player(id = 5, name = "Player 5", role = Role.DOCTOR),
            Player(id = 6, name = "Player 6", role = Role.ESCORT),
            Player(id = 7, name = "Player 7", role = Role.MANIAC),
            Player(id = 8, name = "Player 8", role = Role.CIVILIAN)
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
            phase = Phase.NIGHT_MAFIA
        )
    }
}