package neuromafia.bot

import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Player
import neuromafia.core.model.Role
import neuromafia.llm.LlmLanguage
import neuromafia.llm.LlmProvider
import neuromafia.llm.LlmResponse

class LlmPlayerControllerTest {
    @Test
    fun `chooseDaySpeech should parse llm speech and nomination`() {
        val provider = mockk<LlmProvider>()

        coEvery {
            provider.ask(any())
        } returns LlmResponse(
            content = """
                {
                  "publicReasoning": "Player 4 looks suspicious.",
                  "speech": "I think Player 4 is mafia.",
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent()
        )

        val controller = LlmPlayerController(
            provider = provider,
            language = LlmLanguage.EN
        )

        val action = controller.chooseDaySpeech(
            state = testState(),
            playerId = 1
        )

        assertEquals(1, action.playerId)
        assertEquals("I think Player 4 is mafia.", action.message)
        assertEquals(4, action.nominatedPlayerId)
    }

    @Test
    fun `chooseDayVote should parse llm vote`() {
        val provider = mockk<LlmProvider>()

        coEvery {
            provider.ask(any())
        } returns LlmResponse(
            content = """
                {
                  "publicReasoning": "Player 4 was nominated and behaved suspiciously.",
                  "speech": null,
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent()
        )

        val controller = LlmPlayerController(
            provider = provider,
            language = LlmLanguage.EN
        )

        val action = controller.chooseDayVote(
            state = testState(),
            playerId = 1,
            nominatedPlayerIds = listOf(3, 4)
        )

        assertEquals(1, action.voterId)
        assertEquals(4, action.targetId)
    }

    @Test
    fun `chooseMafiaKillVote should parse llm mafia kill target`() {
        val provider = mockk<LlmProvider>()

        coEvery {
            provider.ask(any())
        } returns LlmResponse(
            content = """
                {
                  "publicReasoning": "Player 1 is an active civilian voice.",
                  "speech": null,
                  "targetId": 1,
                  "skip": false
                }
            """.trimIndent()
        )

        val controller = LlmPlayerController(
            provider = provider,
            language = LlmLanguage.EN
        )

        val action = controller.chooseMafiaKillVote(
            state = testState(),
            playerId = 3
        )

        assertEquals(3, action.mafiaId)
        assertEquals(1, action.targetId)
    }

    @Test
    fun `chooseDaySpeech should support russian language response`() {
        val provider = mockk<LlmProvider>()

        coEvery {
            provider.ask(any())
        } returns LlmResponse(
            content = """
                {
                  "publicReasoning": "Игрок 4 говорит слишком осторожно.",
                  "speech": "Я подозреваю игрока 4.",
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent()
        )

        val controller = LlmPlayerController(
            provider = provider,
            language = LlmLanguage.RU
        )

        val action = controller.chooseDaySpeech(
            state = testState(),
            playerId = 1
        )

        assertEquals("Я подозреваю игрока 4.", action.message)
        assertEquals(4, action.nominatedPlayerId)
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
            players = players
        )
    }

    @Test
    fun `chooseDaySpeech should ignore killed nominated target`() {
        val provider = mockk<LlmProvider>()

        coEvery {
            provider.ask(any())
        } returns LlmResponse(
            content = """
            {
              "publicReasoning": "I suspect Player 4.",
              "speech": "I nominate Player 4.",
              "targetId": 4,
              "skip": false
            }
        """.trimIndent()
        )

        val controller = LlmPlayerController(
            provider = provider,
            language = LlmLanguage.EN
        )

        val state = testState().copy(
            players = testState().players.map { player ->
                if (player.id == 4) {
                    player.copy(alive = false)
                } else {
                    player
                }
            }
        )

        val action = controller.chooseDaySpeech(
            state = state,
            playerId = 1
        )

        assertEquals("I nominate Player 4.", action.message)
        assertEquals(null, action.nominatedPlayerId)
    }

    @Test
    fun `chooseDayVote should skip vote for non nominated target`() {
        val provider = mockk<LlmProvider>()

        coEvery {
            provider.ask(any())
        } returns LlmResponse(
            content = """
            {
              "publicReasoning": "I want to vote Player 5.",
              "speech": null,
              "targetId": 5,
              "skip": false
            }
        """.trimIndent()
        )

        val controller = LlmPlayerController(
            provider = provider,
            language = LlmLanguage.EN
        )

        val action = controller.chooseDayVote(
            state = testState(),
            playerId = 1,
            nominatedPlayerIds = listOf(3, 4)
        )

        assertEquals(1, action.voterId)
        assertEquals(null, action.targetId)
    }
}