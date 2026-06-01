package neuromafia.llm

import kotlin.test.Test
import kotlin.test.assertTrue
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class PromptHistoryFormatterTest {
    @Test
    fun `formatPublicHistory should include russian speech and nomination`() {
        val state = testState(
            eventLog = listOf(
                GameEvent.PlayerSpoke(
                    playerId = 2,
                    message = "Я подозреваю игрока 4."
                ),
                GameEvent.PlayerNominated(
                    speakerId = 2,
                    nominatedPlayerId = 4
                )
            )
        )

        val result = PromptHistoryFormatter().formatPublicHistory(
            state = state,
            language = LlmLanguage.RU
        )

        assertTrue(result.contains("Игрок 2 сказал: Я подозреваю игрока 4."))
        assertTrue(result.contains("Игрок 2 выставил игрока 4."))
    }

    @Test
    fun `formatPublicHistory should include killed player without role`() {
        val state = testState(
            eventLog = listOf(
                GameEvent.PlayerKilled(
                    playerId = 4,
                    reason = KillReason.DAY_VOTE
                )
            )
        )

        val result = PromptHistoryFormatter().formatPublicHistory(
            state = state,
            language = LlmLanguage.RU
        )

        assertTrue(result.contains("Игрок 4 выбыл"))
        assertTrue(result.contains("Роль не раскрыта"))
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
}