package neuromafia

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import neuromafia.app.MafiaApplication
import neuromafia.core.engine.GameFactory
import neuromafia.core.engine.WinConditionChecker
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.dev.DevLog
import neuromafia.msg.Language
import neuromafia.msg.Messages
import neuromafia.present.PublicEventPrinter

import neuromafia.llm.LlmLanguage
import neuromafia.llm.StubLlmProvider
import neuromafia.llm.openrouter.HttpClientFactory
import neuromafia.llm.openrouter.OpenRouterProvider

class NeuromafiaCommand : CliktCommand(
    name = "neuromafia"
) {
    override fun help(context: Context): String {
        return "CLI simulator for Mafia game with bots and LLM players."
    }

    private val language: String by option(
        "--lang",
        help = "Interface language: en or ru."
    ).choice("en", "ru").default("en")

    private val debug: Boolean by option(
        "--debug",
        help = "Enable developer debug logs."
    ).flag(default = false)

    private val runGame: Boolean by option(
        "--run-game",
        help = "Run full game simulation."
    ).flag(default = false)

    private val bot: String by option(
        "--bot",
        help = "Bot type: random, llm or stub."
    ).choice("random", "llm", "stub").default("random")

    private val maxRounds: Int by option(
        "--max-rounds",
        help = "Maximum number of game rounds."
    ).int().default(20)

    private val mode: String by option(
        "--mode",
        help = "Game mode: observe or human."
    ).choice("observe", "human").default("observe")

    private val players: Int by option(
        "--players",
        help = "Total number of players."
    ).int().default(10)

    private val mafia: Int by option(
        "--mafia",
        help = "Number of mafia players including Godfather."
    ).int().default(3)

    private val noCommissar: Boolean by option(
        "--no-commissar",
        help = "Disable commissar role."
    ).flag(default = false)

    private val doctor: Boolean by option(
        "--doctor",
        help = "Enable doctor role."
    ).flag(default = false)

    private val maniac: Boolean by option(
        "--maniac",
        help = "Enable maniac role."
    ).flag(default = false)

    private val escort: Boolean by option(
        "--escort",
        help = "Enable escort role."
    ).flag(default = false)

    private val provider: String by option(
        "--provider",
        help = "LLM provider name."
    ).default("stub")

    private val model: String by option(
        "--model",
        help = "LLM model name."
    ).default("stub")

    private val humanPlayer: Int by option(
        "--human-player",
        help = "Human player id. Used only in human mode."
    ).int().default(1)

    override fun run() {
        DevLog.enabled = debug

        val selectedLanguage = Language.fromCliValue(language)
        val messages = Messages(selectedLanguage)

        val gameMode = when (mode) {
            "observe" -> GameMode.OBSERVE
            "human" -> GameMode.HUMAN
            else -> error("Unknown game mode: $mode")
        }

        val config = GameConfig(
            mode = gameMode,
            playerCount = players,
            mafiaCount = mafia,
            commissarEnabled = !noCommissar,
            doctorEnabled = doctor,
            maniacEnabled = maniac,
            escortEnabled = escort,
            provider = provider,
            model = model,
            humanPlayerId = if (gameMode == GameMode.HUMAN) humanPlayer else null
        )

        echo(messages.appStarted())
        echo(messages.gameConfig(config))

        if (!runGame) {
            printCreatedGame(config)
            return
        }

        val finalState = when (bot) {
            "random" -> MafiaApplication().runRandomGame(
                config = config,
                maxRounds = maxRounds
            )

            "stub" -> MafiaApplication().runLlmGame(
                config = config,
                maxRounds = maxRounds,
                provider = StubLlmProvider(
                    response = """
                {
                  "publicReasoning": "Stub LLM chooses Player 1 as a fallback.",
                  "speech": "I am watching carefully.",
                  "targetId": 1,
                  "skip": false
                }
            """.trimIndent()
                ),
                language = LlmLanguage.fromCliValue(language)
            )

            "llm" -> {
                require(provider == "openrouter") {
                    "Only OpenRouter provider is supported now. Use --provider openrouter."
                }

                val httpClient = HttpClientFactory.create()

                try {
                    MafiaApplication().runLlmGame(
                        config = config,
                        maxRounds = maxRounds,
                        provider = OpenRouterProvider(
                            apiKey = OpenRouterProvider.apiKeyFromEnvironment(),
                            model = model,
                            client = httpClient
                        ),
                        language = LlmLanguage.fromCliValue(language)
                    )
                } finally {
                    httpClient.close()
                }
            }

            else -> error("Unknown bot type: $bot")
        }

        PublicEventPrinter().printPublicEvents(finalState)
        PublicEventPrinter().printGameSummary(finalState)
    }

    private fun printCreatedGame(config: GameConfig) {
        DevLog.info("Creating game state without running full game")

        val state = GameFactory.create(config)
        val winner = WinConditionChecker.checkWinner(state)

        echo("")
        echo("Created game:")

        state.players.forEach { player ->
            if (DevLog.enabled) {
                echo("  ${player.id}. ${player.name} — ${player.role}")
            } else {
                echo("  ${player.id}. ${player.name}")
            }
        }

        echo("")
        echo("Winner at start: ${winner ?: "none"}")
        echo("")
        echo("Use --run-game to run full simulation.")
    }
}

fun main(args: Array<String>) {
    NeuromafiaCommand().main(args)
}