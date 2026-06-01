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

class GameEngineNominationTest {
    @Test
    fun `nominatePlayer should add player to nominated list`() {
        val state = testState()

        val updatedState = GameEngine.nominatePlayer(
            state = state,
            speakerId = 1,
            nominatedPlayerId = 4
        )

        assertEquals(listOf(4), updatedState.nominatedPlayerIds)
    }

    @Test
    fun `nominatePlayer should add nomination event`() {
        val state = testState()

        val updatedState = GameEngine.nominatePlayer(
            state = state,
            speakerId = 1,
            nominatedPlayerId = 4
        )

        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerNominated(
                    speakerId = 1,
                    nominatedPlayerId = 4
                )
            )
        )
    }

    @Test
    fun `nominatePlayer should not allow self nomination`() {
        val state = testState()

        assertFailsWith<IllegalArgumentException> {
            GameEngine.nominatePlayer(
                state = state,
                speakerId = 1,
                nominatedPlayerId = 1
            )
        }
    }

    @Test
    fun `nominatePlayer should not allow killed speaker`() {
        val state = testState(
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN, alive = false),
                Player(id = 2, name = "Player 2", role = Role.CIVILIAN),
                Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
                Player(id = 4, name = "Player 4", role = Role.GODFATHER)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            GameEngine.nominatePlayer(
                state = state,
                speakerId = 1,
                nominatedPlayerId = 4
            )
        }
    }

    @Test
    fun `nominatePlayer should not allow killed nominated player`() {
        val state = testState(
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
                Player(id = 2, name = "Player 2", role = Role.CIVILIAN),
                Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
                Player(id = 4, name = "Player 4", role = Role.GODFATHER, alive = false)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            GameEngine.nominatePlayer(
                state = state,
                speakerId = 1,
                nominatedPlayerId = 4
            )
        }
    }

    @Test
    fun `nominatePlayer should not allow duplicate nomination`() {
        val state = testState(
            nominatedPlayerIds = listOf(4)
        )

        assertFailsWith<IllegalArgumentException> {
            GameEngine.nominatePlayer(
                state = state,
                speakerId = 1,
                nominatedPlayerId = 4
            )
        }
    }

    @Test
    fun `nominatePlayer should work only during day discussion`() {
        val state = testState(
            phase = Phase.DAY_VOTING
        )

        assertFailsWith<IllegalArgumentException> {
            GameEngine.nominatePlayer(
                state = state,
                speakerId = 1,
                nominatedPlayerId = 4
            )
        }
    }

    private fun testState(
        players: List<Player> = defaultPlayers(),
        phase: Phase = Phase.DAY_DISCUSSION,
        nominatedPlayerIds: List<Int> = emptyList()
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