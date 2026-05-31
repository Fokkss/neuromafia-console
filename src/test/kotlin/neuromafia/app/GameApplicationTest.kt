package neuromafia.app

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.core.model.Phase

class GameApplicationTest {
    @Test
    fun `runRandomGame should return finished game or stop at max rounds`() {
        val config = GameConfig(
            mode = GameMode.OBSERVE,
            playerCount = 10,
            mafiaCount = 3,
            commissarEnabled = true,
            doctorEnabled = true,
            maniacEnabled = true,
            escortEnabled = true,
            provider = "stub",
            model = "stub",
            humanPlayerId = null
        )

        val finalState = MafiaApplication().runRandomGame(
            config = config,
            maxRounds = 20,
            random = Random(1)
        )

        assertTrue(
            finalState.finished ||
                    finalState.phase == Phase.DAY_DISCUSSION
        )

        assertTrue(finalState.eventLog.isNotEmpty())
    }
}