package neuromafia.core.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class PhaseManagerTest {
    @Test
    fun `startDayVoting should move from discussion to voting`() {
        val state = testState(
            phase = Phase.DAY_DISCUSSION
        )

        val updatedState = PhaseManager.startDayVoting(state)

        assertEquals(Phase.DAY_VOTING, updatedState.phase)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PhaseChanged(
                    from = Phase.DAY_DISCUSSION,
                    to = Phase.DAY_VOTING,
                    dayNumber = 1
                )
            )
        )
    }

    @Test
    fun `startDayVoting should fail outside discussion phase`() {
        val state = testState(
            phase = Phase.DAY_VOTING
        )

        assertFailsWith<IllegalArgumentException> {
            PhaseManager.startDayVoting(state)
        }
    }

    @Test
    fun `startNight should always move to mafia night`() {
        val state = testState(
            phase = Phase.DAY_VOTING,
            commissarEnabled = false,
            doctorEnabled = false,
            maniacEnabled = false,
            escortEnabled = false
        )

        val updatedState = PhaseManager.startNight(state)

        assertEquals(Phase.NIGHT_MAFIA, updatedState.phase)
    }

    @Test
    fun `night flow should always use full closed mafia order`() {
        val state = testState(
            phase = Phase.DAY_VOTING,
            commissarEnabled = false,
            doctorEnabled = false,
            maniacEnabled = false,
            escortEnabled = false
        )

        val mafiaNightState = PhaseManager.startNight(state)
        val godfatherNightState = PhaseManager.finishCurrentNightPhase(mafiaNightState)
        val doctorNightState = PhaseManager.finishCurrentNightPhase(godfatherNightState)
        val commissarNightState = PhaseManager.finishCurrentNightPhase(doctorNightState)
        val escortNightState = PhaseManager.finishCurrentNightPhase(commissarNightState)
        val maniacNightState = PhaseManager.finishCurrentNightPhase(escortNightState)
        val nextDayState = PhaseManager.finishCurrentNightPhase(maniacNightState)

        assertEquals(Phase.NIGHT_MAFIA, mafiaNightState.phase)
        assertEquals(Phase.NIGHT_GODFATHER, godfatherNightState.phase)
        assertEquals(Phase.NIGHT_DOCTOR, doctorNightState.phase)
        assertEquals(Phase.NIGHT_COMMISSAR, commissarNightState.phase)
        assertEquals(Phase.NIGHT_ESCORT, escortNightState.phase)
        assertEquals(Phase.NIGHT_MANIAC, maniacNightState.phase)
        assertEquals(Phase.DAY_DISCUSSION, nextDayState.phase)
        assertEquals(2, nextDayState.dayNumber)
    }

    @Test
    fun `start next day should clear nominations`() {
        val state = testState(
            phase = Phase.NIGHT_MANIAC,
            nominatedPlayerIds = listOf(2, 3)
        )

        val updatedState = PhaseManager.finishCurrentNightPhase(state)

        assertEquals(Phase.DAY_DISCUSSION, updatedState.phase)
        assertEquals(emptyList(), updatedState.nominatedPlayerIds)
        assertEquals(2, updatedState.dayNumber)
    }

    @Test
    fun `finishCurrentNightPhase should fail outside night phase`() {
        val state = testState(
            phase = Phase.DAY_DISCUSSION
        )

        assertFailsWith<IllegalStateException> {
            PhaseManager.finishCurrentNightPhase(state)
        }
    }

    private fun testState(
        phase: Phase,
        nominatedPlayerIds: List<Int> = emptyList(),
        commissarEnabled: Boolean = true,
        doctorEnabled: Boolean = false,
        maniacEnabled: Boolean = false,
        escortEnabled: Boolean = false
    ): GameState {
        val players = defaultPlayers()

        return GameState(
            config = GameConfig(
                mode = GameMode.OBSERVE,
                playerCount = players.size,
                mafiaCount = players.count { it.role == Role.MAFIA || it.role == Role.GODFATHER },
                commissarEnabled = commissarEnabled,
                doctorEnabled = doctorEnabled,
                maniacEnabled = maniacEnabled,
                escortEnabled = escortEnabled,
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