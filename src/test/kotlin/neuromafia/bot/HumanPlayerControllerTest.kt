package neuromafia.bot

import kotlin.test.Test
import kotlin.test.assertEquals
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Player
import neuromafia.core.model.Role
import neuromafia.msg.Language

class HumanPlayerControllerTest {
    @Test
    fun `chooseDaySpeech should read speech and nomination`() {
        val io = FakeHumanIo(
            inputs = mutableListOf(
                "Я подозреваю игрока 4.",
                "4"
            )
        )

        val controller = HumanPlayerController(
            language = Language.RU,
            io = io
        )

        val action = controller.chooseDaySpeech(
            state = testState(),
            playerId = 1
        )

        assertEquals(1, action.playerId)
        assertEquals("Я подозреваю игрока 4.", action.message)
        assertEquals(4, action.nominatedPlayerId)
    }

    @Test
    fun `chooseDayVote should allow skip`() {
        val io = FakeHumanIo(
            inputs = mutableListOf("")
        )

        val controller = HumanPlayerController(
            language = Language.RU,
            io = io
        )

        val action = controller.chooseDayVote(
            state = testState(),
            playerId = 1,
            nominatedPlayerIds = listOf(3, 4)
        )

        assertEquals(1, action.voterId)
        assertEquals(null, action.targetId)
    }

    @Test
    fun `chooseMafiaKillVote should read required target`() {
        val io = FakeHumanIo(
            inputs = mutableListOf("1")
        )

        val controller = HumanPlayerController(
            language = Language.RU,
            io = io
        )

        val action = controller.chooseMafiaKillVote(
            state = testState(),
            playerId = 3
        )

        assertEquals(3, action.mafiaId)
        assertEquals(1, action.targetId)
    }

    private fun testState(): GameState {
        val players = listOf(
            Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
            Player(id = 2, name = "Player 2", role = Role.COMMISSAR),
            Player(id = 3, name = "Player 3", role = Role.MAFIA),
            Player(id = 4, name = "Player 4", role = Role.GODFATHER),
            Player(id = 5, name = "Player 5", role = Role.DOCTOR)
        )

        return GameState(
            config = GameConfig(
                mode = GameMode.HUMAN,
                playerCount = players.size,
                mafiaCount = 2,
                commissarEnabled = true,
                doctorEnabled = true,
                maniacEnabled = false,
                escortEnabled = false,
                provider = "stub",
                model = "stub",
                humanPlayerId = 1
            ),
            players = players
        )
    }

    private class FakeHumanIo(
        private val inputs: MutableList<String>
    ) : HumanIo {
        val outputs: MutableList<String> = mutableListOf()

        override fun writeLine(message: String) {
            outputs.add(message)
        }

        override fun readLine(): String? {
            return inputs.removeFirstOrNull()
        }
    }
}