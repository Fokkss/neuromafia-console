package neuromafia.app

import kotlin.random.Random
import neuromafia.bot.PlayerController
import neuromafia.bot.RandomPlayerController
import neuromafia.core.engine.GameFactory
import neuromafia.core.engine.GameLoopRunner
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameState
import neuromafia.dev.DevLog

import neuromafia.bot.LlmPlayerController
import neuromafia.llm.LlmLanguage
import neuromafia.llm.LlmProvider

class MafiaApplication {
    fun runRandomGame(
        config: GameConfig,
        maxRounds: Int,
        random: Random = Random.Default,
        onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }

    ): GameState {
        require(maxRounds > 0) {
            "Max rounds must be positive."
        }

        DevLog.info("Creating initial game state")

        val initialState = GameFactory.create(
            config = config,
            random = random
        )

        val controllersByPlayerId = createRandomControllers(
            state = initialState,
            random = random
        )

        DevLog.info("Starting game loop")

        return GameLoopRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runUntilFinished(
            initialState = initialState,
            maxRounds = maxRounds
        )
    }

    fun runLlmGame(
        config: GameConfig,
        maxRounds: Int,
        provider: LlmProvider,
        language: LlmLanguage,
        onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> }
    ): GameState {
        require(maxRounds > 0) {
            "Max rounds must be positive."
        }

        val initialState = GameFactory.create(config)

        val controllersByPlayerId = initialState.players.associate { player ->
            player.id to LlmPlayerController(
                provider = provider,
                language = language
            ) as PlayerController
        }

        return GameLoopRunner(
            controllersByPlayerId = controllersByPlayerId,
            onStateChanged = onStateChanged
        ).runUntilFinished(
            initialState = initialState,
            maxRounds = maxRounds
        )
    }

    private fun createRandomControllers(
        state: GameState,
        random: Random
    ): Map<Int, PlayerController> {
        return state.players.associate { player ->
            player.id to RandomPlayerController(
                random = Random(random.nextInt())
            )
        }
    }
}