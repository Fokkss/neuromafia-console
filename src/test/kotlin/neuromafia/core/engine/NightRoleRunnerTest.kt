package neuromafia.core.engine

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import neuromafia.bot.PlayerController
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class NightRoleRunnersTest {
    @Test
    fun `doctor night should store protected player`() {
        val state = testState(phase = Phase.NIGHT_DOCTOR)
        val controller = mockk<PlayerController>()

        every {
            controller.chooseDoctorHeal(any(), 5)
        } returns PlayerAction.DoctorHeal(
            doctorId = 5,
            targetId = 1
        )

        val runner = DoctorRunner(
            controllersByPlayerId = mapOf(5 to controller)
        )

        val updatedState = runner.runDoctorNight(state)

        assertEquals(1, updatedState.protectedPlayerId)
    }

    @Test
    fun `commissar night should store mafia check result`() {
        val state = testState(phase = Phase.NIGHT_COMMISSAR)
        val controller = mockk<PlayerController>()

        every {
            controller.chooseCommissarCheck(any(), 2)
        } returns PlayerAction.CommissarCheck(
            commissarId = 2,
            targetId = 4
        )

        val runner = CommissarRunner(
            controllersByPlayerId = mapOf(2 to controller)
        )

        val updatedState = runner.runCommissarNight(state)

        assertEquals(true, updatedState.commissarChecks[4])
    }

    @Test
    fun `escort night should store visited player`() {
        val state = testState(phase = Phase.NIGHT_ESCORT)
        val controller = mockk<PlayerController>()

        every {
            controller.chooseEscortVisit(any(), 6)
        } returns PlayerAction.EscortVisit(
            escortId = 6,
            targetId = 7
        )

        val runner = EscortRunner(
            controllersByPlayerId = mapOf(6 to controller)
        )

        val updatedState = runner.runEscortNight(state)

        assertEquals(7, updatedState.escortVisitedPlayerId)
    }

    @Test
    fun `maniac night should store pending maniac kill target`() {
        val state = testState(phase = Phase.NIGHT_MANIAC)
        val controller = mockk<PlayerController>()

        every {
            controller.chooseManiacKill(any(), 7)
        } returns PlayerAction.ManiacKill(
            maniacId = 7,
            targetId = 1
        )

        val runner = ManiacRunner(
            controllersByPlayerId = mapOf(7 to controller)
        )

        val updatedState = runner.runManiacNight(state)

        assertEquals(1, updatedState.pendingManiacKillTargetId)
    }

    private fun testState(
        phase: Phase
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
            phase = phase
        )
    }
}