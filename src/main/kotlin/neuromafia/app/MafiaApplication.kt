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

import neuromafia.bot.HumanPlayerController
import neuromafia.core.model.GameMode
import neuromafia.msg.Language

class MafiaApplication {
    fun runRandomGame(
        config: GameConfig,
        maxRounds: Int,
        random: Random = Random.Default,
        onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> },
        language: Language = Language.EN
    ): GameState {
        require(maxRounds > 0) {
            "max rounds must be positive."
        }

        DevLog.info("creating initial game state")

        val initialState = GameFactory.create(
            config = config,
            random = random
        )

        // one -- actual human
        // others -- random
        val controllersByPlayerId = initialState.players.associate { player ->
            val controller = if (
                config.mode == GameMode.HUMAN &&
                config.humanPlayerId == player.id
            ) {
                HumanPlayerController(
                    language = language
                )
            } else {
                RandomPlayerController(
                    random = Random(random.nextInt())
                )
            }

            player.id to controller
        }

        DevLog.info("starting game loop")

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
        onStateChanged: (previousState: GameState, currentState: GameState) -> Unit = { _, _ -> },
        uiLanguage: Language = Language.EN,
    ): GameState {
        require(maxRounds > 0) {
            "max rounds must be positive."
        }

        val initialState = GameFactory.create(config)

        val controllersByPlayerId = initialState.players.associate { player ->
            val controller = if (
                config.mode == GameMode.HUMAN &&
                config.humanPlayerId == player.id
            ) {
                HumanPlayerController(
                    language = uiLanguage
                )
            } else {
                LlmPlayerController(
                    provider = provider,
                    language = language
                )
            }

            player.id to controller
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