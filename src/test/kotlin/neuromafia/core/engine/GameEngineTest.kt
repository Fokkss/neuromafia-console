package neuromafia.core.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import neuromafia.core.model.KillReason
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role
import neuromafia.core.model.Winner

class GameEngineTest {
    @Test
    fun `killPlayer should mark player as not alive`() {
        val state = defaultState()

        val updatedState = GameEngine.killPlayer(
            state = state,
            playerId = 1,
            reason = KillReason.DEBUG
        )

        assertFalse(updatedState.playerById(1).alive)
    }

    @Test
    fun `killPlayer should keep other players alive`() {
        val state = defaultState()

        val updatedState = GameEngine.killPlayer(
            state = state,
            playerId = 1,
            reason = KillReason.DEBUG
        )

        assertTrue(updatedState.playerById(2).alive)
        assertTrue(updatedState.playerById(3).alive)
        assertTrue(updatedState.playerById(4).alive)
    }

    @Test
    fun `killPlayer should add event to log`() {
        val state = defaultState()

        val updatedState = GameEngine.killPlayer(
            state = state,
            playerId = 1,
            reason = KillReason.DEBUG
        )

        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerKilled(
                    playerId = 1,
                    reason = KillReason.DEBUG
                )
            )
        )
    }

    @Test
    fun `killPlayer should throw when player does not exist`() {
        val state = defaultState()

        assertFailsWith<IllegalStateException> {
            GameEngine.killPlayer(
                state = state,
                playerId = 999,
                reason = KillReason.DEBUG
            )
        }
    }

    @Test
    fun `killPlayer should throw when player is already eliminated`() {
        val state = defaultState().copy(
            players = defaultState().players.map { player ->
                if (player.id == 1) {
                    player.copy(alive = false)
                } else {
                    player
                }
            }
        )

        assertFailsWith<IllegalArgumentException> {
            GameEngine.killPlayer(
                state = state,
                playerId = 1,
                reason = KillReason.DEBUG
            )
        }
    }

    @Test
    fun `killPlayer should set civilians winner when last mafia is eliminated`() {
        val state = defaultState()

        val updatedState = GameEngine.killPlayer(
            state = state,
            playerId = 4,
            reason = KillReason.DEBUG
        )

        assertEquals(Winner.CIVILIANS, updatedState.winner)
        assertEquals(Phase.FINISHED, updatedState.phase)
    }

    @Test
    fun `killPlayer should set mafia winner when mafia count becomes equal to civilians count`() {
        val state = stateWithTwoMafiaAndTwoCivilians()

        val updatedState = GameEngine.killPlayer(
            state = state,
            playerId = 1,
            reason = KillReason.DEBUG
        )

        assertEquals(Winner.MAFIA, updatedState.winner)
        assertEquals(Phase.FINISHED, updatedState.phase)
    }

    private fun defaultState(): GameState {
        return GameState(
            config = defaultConfig(
                playerCount = 4,
                mafiaCount = 1
            ),
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
                Player(id = 2, name = "Player 2", role = Role.CIVILIAN),
                Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
                Player(id = 4, name = "Player 4", role = Role.MAFIA)
            )
        )
    }

    private fun stateWithTwoMafiaAndTwoCivilians(): GameState {
        return GameState(
            config = defaultConfig(
                playerCount = 4,
                mafiaCount = 2
            ),
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
                Player(id = 2, name = "Player 2", role = Role.COMMISSAR),
                Player(id = 3, name = "Player 3", role = Role.MAFIA),
                Player(id = 4, name = "Player 4", role = Role.GODFATHER)
            )
        )
    }

    private fun defaultConfig(
        playerCount: Int,
        mafiaCount: Int
    ): GameConfig {
        return GameConfig(
            mode = GameMode.OBSERVE,
            playerCount = playerCount,
            mafiaCount = mafiaCount,
            commissarEnabled = true,
            doctorEnabled = false,
            maniacEnabled = false,
            escortEnabled = false,
            provider = "stub",
            model = "stub",
            humanPlayerId = null
        )
    }
}