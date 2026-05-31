package neuromafia.core.engine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import neuromafia.bot.PlayerController
import neuromafia.bot.RandomPlayerController
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class GameRoundRunnerTest {
    @Test
    fun `runRound should move game to next day or finish game`() {
        val state = testState()
        val controllers = state.players.associate { player ->
            player.id to RandomPlayerController(
                random = Random(player.id)
            ) as PlayerController
        }

        val runner = GameRoundRunner(
            controllersByPlayerId = controllers
        )

        val updatedState = runner.runRound(state)

        assertTrue(
            updatedState.phase == Phase.DAY_DISCUSSION ||
                    updatedState.phase == Phase.FINISHED
        )

        if (!updatedState.finished) {
            assertEquals(2, updatedState.dayNumber)
        }
    }

    @Test
    fun `runRound should create game events`() {
        val state = testState()
        val controllers = state.players.associate { player ->
            player.id to RandomPlayerController(
                random = Random(player.id + 100)
            ) as PlayerController
        }

        val runner = GameRoundRunner(
            controllersByPlayerId = controllers
        )

        val updatedState = runner.runRound(state)

        assertTrue(updatedState.eventLog.isNotEmpty())
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
            Player(id = 8, name = "Player 8", role = Role.CIVILIAN),
            Player(id = 9, name = "Player 9", role = Role.CIVILIAN),
            Player(id = 10, name = "Player 10", role = Role.CIVILIAN)
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
            phase = Phase.DAY_DISCUSSION
        )
    }
}