package neuromafia.bot

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class DaySpeechControllerMockTest {
    @Test
    fun `mocked controller should return speech with nomination`() {
        val controller = mockk<PlayerController>()
        val state = testState()

        every {
            controller.chooseDaySpeech(
                state = state,
                playerId = 1
            )
        } returns PlayerAction.DaySpeech(
            playerId = 1,
            message = "I suspect Player 4.",
            nominatedPlayerId = 4
        )

        val action = controller.chooseDaySpeech(
            state = state,
            playerId = 1
        )

        assertEquals(1, action.playerId)
        assertEquals("I suspect Player 4.", action.message)
        assertEquals(4, action.nominatedPlayerId)
    }

    private fun testState(): GameState {
        return GameState(
            config = GameConfig(
                mode = GameMode.OBSERVE,
                playerCount = 4,
                mafiaCount = 1,
                commissarEnabled = true,
                doctorEnabled = false,
                maniacEnabled = false,
                escortEnabled = false,
                provider = "stub",
                model = "stub",
                humanPlayerId = null
            ),
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
                Player(id = 2, name = "Player 2", role = Role.CIVILIAN),
                Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
                Player(id = 4, name = "Player 4", role = Role.GODFATHER)
            )
        )
    }
}