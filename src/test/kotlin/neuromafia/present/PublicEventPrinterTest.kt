package neuromafia.present

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertTrue
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role
import neuromafia.msg.Language

class PublicEventPrinterTest {
    @Test
    fun `printPublicEvents should print russian player speech`() {
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

        val output = captureStdout {
            PublicEventPrinter(Language.RU).printPublicEvents(state)
        }

        assertTrue(output.contains("Публичный лог игры"))
        assertTrue(output.contains("Игрок 2: Я подозреваю игрока 4."))
        assertTrue(output.contains("Игрок 2 выставил игрока 4."))
    }

    @Test
    fun `printPublicEvents should print english player speech`() {
        val state = testState(
            eventLog = listOf(
                GameEvent.PlayerSpoke(
                    playerId = 2,
                    message = "I suspect Player 4."
                ),
                GameEvent.PlayerNominated(
                    speakerId = 2,
                    nominatedPlayerId = 4
                )
            )
        )

        val output = captureStdout {
            PublicEventPrinter(Language.EN).printPublicEvents(state)
        }

        assertTrue(output.contains("Public game log"))
        assertTrue(output.contains("Player 2: I suspect Player 4."))
        assertTrue(output.contains("Player 2 nominated Player 4."))
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
            phase = Phase.DAY_DISCUSSION,
            eventLog = eventLog
        )
    }

    private fun captureStdout(
        block: () -> Unit
    ): String {
        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()

        System.setOut(PrintStream(outputStream))

        try {
            block()
        } finally {
            System.setOut(originalOut)
        }

        return outputStream.toString()
    }
}