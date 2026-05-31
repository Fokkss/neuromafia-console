package neuromafia

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int

import neuromafia.core.engine.GameFactory
import neuromafia.core.engine.WinConditionChecker
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameMode
import neuromafia.dev.DevLog
import neuromafia.msg.Language
import neuromafia.msg.Messages
import neuromafia.core.engine.GameEngine
import neuromafia.core.model.KillReason

class NeuromafiaCommand : CliktCommand(
    name = "neuromafia"
) {
    override fun help(context: Context): String {
        return "CLI simulator for Mafia game with LLM bots."
    }

    private val language: String by option(
        "--lang",
        help = "Interface language: en or ru."
    ).choice("en", "ru").default("en")

    private val debug: Boolean by option(
        "--debug",
        help = "Enable developer debug logs."
    ).flag(default = false)

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

        DevLog.info("Creating game state")

        val state = GameFactory.create(config)

        DevLog.info("Players generated: ${state.players.size}")
        DevLog.info("Current phase: ${state.phase}")

        val winner = WinConditionChecker.checkWinner(state)

        echo("")
        echo(messages.createdGame())

        state.players.forEach { player ->
            echo("  ${messages.playerLine(player)}")
        }

        echo("")
        echo(messages.winnerAtStart(winner))
    }
}

fun main(args: Array<String>) {
    NeuromafiaCommand().main(args)
}