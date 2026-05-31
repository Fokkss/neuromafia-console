package neuromafia.msg

import neuromafia.core.model.GameConfig
import neuromafia.core.model.Player

class Messages(
    private val language: Language
) {
    fun appStarted(): String {
        return when (language) {
            Language.EN -> "neuromafia started!"
            Language.RU -> "neuromafia началась!"
        }
    }

    fun gameConfig(config: GameConfig): String {
        return when (language) {
            Language.EN -> """
                Game config:
                  mode: ${config.mode}
                  players: ${config.playerCount}
                  mafia: ${config.mafiaCount}
                  commissar: ${config.commissarEnabled}
                  doctor: ${config.doctorEnabled}
                  maniac: ${config.maniacEnabled}
                  escort: ${config.escortEnabled}
                  provider: ${config.provider}
                  model: ${config.model}
                  human player id: ${config.humanPlayerId ?: "none"}
            """.trimIndent()

            Language.RU -> """
                Конфигурация игры:
                  режим: ${config.mode}
                  игроков: ${config.playerCount}
                  мафии: ${config.mafiaCount}
                  комиссар: ${config.commissarEnabled}
                  доктор: ${config.doctorEnabled}
                  маньяк: ${config.maniacEnabled}
                  проститутка: ${config.escortEnabled}
                  провайдер: ${config.provider}
                  модель: ${config.model}
                  id человека: ${config.humanPlayerId ?: "нет"}
            """.trimIndent()
        }
    }

    fun createdGame(): String {
        return when (language) {
            Language.EN -> "Created game:"
            Language.RU -> "Созданная игра:"
        }
    }

    fun playerLine(player: Player): String {
        return when (language) {
            Language.EN -> "${player.id}. ${player.name} — ${player.role}"
            Language.RU -> "${player.id}. ${player.name} — ${roleName(player.role.name)}"
        }
    }

    fun winnerAtStart(winner: Any?): String {
        return when (language) {
            Language.EN -> "Winner at start: ${winner ?: "none"}"
            Language.RU -> "Победитель в начале игры: ${winner ?: "нет"}"
        }
    }

    private fun roleName(roleName: String): String {
        return when (roleName) {
            "CIVILIAN" -> "мирный житель"
            "MAFIA" -> "мафия"
            "GODFATHER" -> "дон мафии"
            "COMMISSAR" -> "комиссар"
            "DOCTOR" -> "доктор"
            "MANIAC" -> "маньяк"
            "ESCORT" -> "проститутка"
            else -> roleName
        }
    }
}