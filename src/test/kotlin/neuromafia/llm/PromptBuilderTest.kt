package neuromafia.llm

import kotlin.test.Test
import kotlin.test.assertTrue
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class PromptBuilderTest {
    @Test
    fun `buildDayVoteRequest should include public history`() {
        val state = testState(
            eventLog = listOf(
                GameEvent.PlayerSpoke(
                    playerId = 2,
                    message = "Я подозреваю игрока 4."
                )
            )
        )

        val request = PromptBuilder().buildDayVoteRequest(
            state = state,
            playerId = 1,
            nominatedPlayerIds = listOf(4),
            language = LlmLanguage.RU
        )

        assertTrue(request.userPrompt.contains("Public history"))
        assertTrue(request.userPrompt.contains("Игрок 2 сказал: Я подозреваю игрока 4."))
    }

    private fun testState(
        eventLog: List<GameEvent>
    ): GameState {
        val players = listOf(
            Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
            Player(id = 2, name = "Player 2", role = Role.COMMISSAR),
            Player(id = 3, name = "Player 3", role = Role.MAFIA),
            Player(id = 4, name = "Player 4", role = Role.GODFATHER)
        )

        return GameState(
            config = GameConfig(
                mode = GameMode.OBSERVE,
                playerCount = players.size,
                mafiaCount = 2,
                commissarEnabled = true,
                doctorEnabled = false,
                maniacEnabled = false,
                escortEnabled = false,
                provider = "stub",
                model = "stub",
                humanPlayerId = null
            ),
            players = players,
            eventLog = eventLog
        )
    }

    @Test
    fun `buildDaySpeechRequest should include current day discussion`() {
        val state = testState(
            eventLog = listOf(
                GameEvent.PlayerSpoke(
                    playerId = 1,
                    message = "Добрый день! Скажите МЯУ, если слышите меня!"
                )
            )
        )

        val request = PromptBuilder().buildDaySpeechRequest(
            state = state,
            playerId = 2,
            language = LlmLanguage.RU
        )

        assertTrue(request.userPrompt.contains("Current day discussion so far"))
        assertTrue(request.userPrompt.contains("Игрок 1 сказал: Добрый день! Скажите МЯУ"))
        assertTrue(request.userPrompt.contains("Do not ignore previous public speeches"))
    }
}