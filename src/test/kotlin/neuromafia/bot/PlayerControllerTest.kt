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

class PlayerControllerTest {
    @Test
    fun `mocked controller should return day vote`() {
        val controller = mockk<PlayerController>()
        val state = testState()

        every {
            controller.chooseDayVote(
                state = state,
                playerId = 1,
                nominatedPlayerIds = listOf(2, 3)
            )
        } returns PlayerAction.DayVote(
            voterId = 1,
            targetId = 3
        )

        val action = controller.chooseDayVote(
            state = state,
            playerId = 1,
            nominatedPlayerIds = listOf(2, 3)
        )

        assertEquals(1, action.voterId)
        assertEquals(3, action.targetId)
    }

    @Test
    fun `mocked controller should return escort block action`() {
        val controller = mockk<PlayerController>()
        val state = testState()

        every {
            controller.chooseEscortVisit(
                state = state,
                playerId = 2
            )
        } returns PlayerAction.EscortVisit(
            escortId = 2,
            targetId = 4
        )

        val action = controller.chooseEscortVisit(
            state = state,
            playerId = 2
        )

        assertEquals(2, action.escortId)
        assertEquals(4, action.targetId)
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
                escortEnabled = true,
                provider = "stub",
                model = "stub",
                humanPlayerId = null
            ),
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
                Player(id = 2, name = "Player 2", role = Role.ESCORT),
                Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
                Player(id = 4, name = "Player 4", role = Role.GODFATHER)
            )
        )
    }
}