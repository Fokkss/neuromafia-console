package neuromafia.core.engine

import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import neuromafia.bot.PlayerController
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameMode
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class DayDiscussionRunnerTest {
    @Test
    fun `runDiscussion should record speeches from all alive players`() {
        val state = testState()
        val controller1 = mockSpeechController(
            playerId = 1,
            message = "I am civilian.",
            nominatedPlayerId = null
        )
        val controller2 = mockSpeechController(
            playerId = 2,
            message = "I suspect Player 4.",
            nominatedPlayerId = 4
        )
        val controller3 = mockSpeechController(
            playerId = 3,
            message = "I will check someone later.",
            nominatedPlayerId = null
        )
        val controller4 = mockSpeechController(
            playerId = 4,
            message = "I am not mafia.",
            nominatedPlayerId = null
        )

        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                1 to controller1,
                2 to controller2,
                3 to controller3,
                4 to controller4
            )
        )

        val updatedState = runner.runDiscussion(state)

        val speechEvents = updatedState.eventLog.filterIsInstance<GameEvent.PlayerSpoke>()

        assertEquals(4, speechEvents.size)
        assertTrue(
            speechEvents.contains(
                GameEvent.PlayerSpoke(
                    playerId = 2,
                    message = "I suspect Player 4."
                )
            )
        )
    }

    @Test
    fun `runDiscussion should apply nominations from speeches`() {
        val state = testState()
        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                1 to mockSpeechController(1, "No nomination.", null),
                2 to mockSpeechController(2, "I nominate Player 4.", 4),
                3 to mockSpeechController(3, "No nomination.", null),
                4 to mockSpeechController(4, "No nomination.", null)
            )
        )

        val updatedState = runner.runDiscussion(state)

        assertEquals(listOf(4), updatedState.nominatedPlayerIds)
        assertTrue(
            updatedState.eventLog.contains(
                GameEvent.PlayerNominated(
                    speakerId = 2,
                    nominatedPlayerId = 4
                )
            )
        )
    }

    @Test
    fun `runDiscussion should skip killed players`() {
        val state = testState(
            players = listOf(
                Player(id = 1, name = "Player 1", role = Role.CIVILIAN),
                Player(id = 2, name = "Player 2", role = Role.CIVILIAN, alive = false),
                Player(id = 3, name = "Player 3", role = Role.COMMISSAR),
                Player(id = 4, name = "Player 4", role = Role.GODFATHER)
            )
        )

        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                1 to mockSpeechController(1, "I am alive.", null),
                3 to mockSpeechController(3, "I am alive too.", null),
                4 to mockSpeechController(4, "I am also alive.", null)
            )
        )

        val updatedState = runner.runDiscussion(state)

        val speechEvents = updatedState.eventLog.filterIsInstance<GameEvent.PlayerSpoke>()

        assertEquals(3, speechEvents.size)
        assertTrue(speechEvents.none { it.playerId == 2 })
    }

    @Test
    fun `runDiscussion should fail when controller is missing`() {
        val state = testState()
        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                1 to mockSpeechController(1, "Text.", null),
                2 to mockSpeechController(2, "Text.", null),
                3 to mockSpeechController(3, "Text.", null)
            )
        )

        assertFailsWith<IllegalStateException> {
            runner.runDiscussion(state)
        }
    }

    @Test
    fun `runDiscussion should fail outside day discussion phase`() {
        val state = testState(
            phase = Phase.DAY_VOTING
        )

        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                1 to mockSpeechController(1, "Text.", null),
                2 to mockSpeechController(2, "Text.", null),
                3 to mockSpeechController(3, "Text.", null),
                4 to mockSpeechController(4, "Text.", null)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runDiscussion(state)
        }
    }

    @Test
    fun `runDiscussion should fail when controller returns wrong player id`() {
        val state = testState()
        val controller = mockk<PlayerController>()

        every {
            controller.chooseDaySpeech(any(), 1)
        } returns PlayerAction.DaySpeech(
            playerId = 999,
            message = "Wrong player id.",
            nominatedPlayerId = null
        )

        val runner = DayDiscussionRunner(
            controllersByPlayerId = mapOf(
                1 to controller,
                2 to mockSpeechController(2, "Text.", null),
                3 to mockSpeechController(3, "Text.", null),
                4 to mockSpeechController(4, "Text.", null)
            )
        )

        assertFailsWith<IllegalArgumentException> {
            runner.runDiscussion(state)
        }
    }

    private fun mockSpeechController(
        playerId: Int,
        message: String,
        nominatedPlayerId: Int?
    ): PlayerController {
        val controller = mockk<PlayerController>()

        every {
            controller.chooseDaySpeech(any(), playerId)
        } returns PlayerAction.DaySpeech(
            playerId = playerId,
            message = message,
            nominatedPlayerId = nominatedPlayerId
        )

        return controller
    }

    private fun testState(
        players: List<Player> = defaultPlayers(),
        phase: Phase = Phase.DAY_DISCUSSION
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
            phase = phase
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