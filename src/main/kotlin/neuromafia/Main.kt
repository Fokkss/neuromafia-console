package neuromafia

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option

class NeuromafiaCommand : CliktCommand(
    name = "neuromafia"
) {
    override fun help(context: Context): String {
        return "CLI simulator for Mafia game with LLM bots."
    }

    private val provider: String by option(
        "--provider",
        help = "LLM provider name."
    ).default("openrouter")

    private val model: String by option(
        "--model",
        help = "LLM model name."
    ).default("gpt-oss")

    override fun run() {
        echo("Neuromafia project started.")
        echo("Provider: $provider")
        echo("Model: $model")
    }
}

fun main(args: Array<String>) {
    NeuromafiaCommand().main(args)
}