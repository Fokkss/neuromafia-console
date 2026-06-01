package neuromafia.present

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Phase
import neuromafia.core.model.Winner
import neuromafia.msg.Language

import neuromafia.core.model.ConsoleColors
import neuromafia.core.model.Player
import neuromafia.core.model.Role

class PublicEventFormatter(
    private val language: Language
) {
    fun formatEvent(event: GameEvent): String? {
        return when (event) {
            is GameEvent.PlayerSpoke -> when (language) {
                Language.EN -> "Player ${event.playerId}: ${event.message}"
                Language.RU -> "Игрок ${event.playerId}: ${event.message}"
            }

            is GameEvent.PlayerNominated -> when (language) {
                Language.EN -> "Player ${event.speakerId} nominated Player ${event.nominatedPlayerId}."
                Language.RU -> "Игрок ${event.speakerId} выставил игрока ${event.nominatedPlayerId}."
            }

            is GameEvent.PlayerVoted -> when (language) {
                Language.EN -> if (event.targetId == null) {
                    "Player ${event.voterId} skipped voting."
                } else {
                    "Player ${event.voterId} voted for Player ${event.targetId}."
                }

                Language.RU -> if (event.targetId == null) {
                    "Игрок ${event.voterId} пропустил голосование."
                } else {
                    "Игрок ${event.voterId} проголосовал за игрока ${event.targetId}."
                }
            }

            is GameEvent.DayVotingTie -> when (language) {
                Language.EN -> "Day voting ended with a tie between players ${event.candidateIds}."
                Language.RU -> "Дневное голосование закончилось ничьёй между игроками ${event.candidateIds}."
            }

            is GameEvent.PlayerKilled -> formatKilledEvent(event)

            is GameEvent.PlayerMuted -> when (language) {
                Language.EN -> "Player ${event.playerId} cannot speak and vote on day ${event.dayNumber}."
                Language.RU -> "Игрок ${event.playerId} не может говорить и голосовать в день ${event.dayNumber}."
            }

            is GameEvent.PhaseChanged ->
                formatPhaseChange(event.from, event.to, event.dayNumber)

            is GameEvent.WinnerDeclared -> when (language) {
                Language.EN -> "Game finished. Winner: ${formatWinner(event.winner)}."
                Language.RU -> "Игра закончена. Победитель: ${formatWinner(event.winner)}."
            }

            // hidden night/private events.
            // (could be possibly seen if I add a special flag for it :) )
            is GameEvent.MafiaKillVoteRecorded -> null
            is GameEvent.MafiaKillTargetSelected -> null
            is GameEvent.MafiaKillTie -> null
            is GameEvent.GodfatherKillDecisionMade -> null
            is GameEvent.GodfatherCommissarChecked -> null
            is GameEvent.DoctorProtected -> null
            is GameEvent.CommissarChecked -> null
            is GameEvent.EscortVisited -> null
            is GameEvent.ManiacKillTargetSelected -> null
        }
    }

    fun formatSummary(state: GameState): List<String> {
        return when (language) {
            Language.EN -> listOf(
                "${ConsoleColors.MAGENTA}Game finished: ${state.finished} ${ConsoleColors.RESET}",
                "${ConsoleColors.MAGENTA}Current day: ${state.dayNumber} ${ConsoleColors.RESET}",
                "${ConsoleColors.MAGENTA}Current phase: ${formatPhase(state.phase)} ${ConsoleColors.RESET}",
                "${ConsoleColors.GREEN}Winner: ${formatWinner(state.winner)} ${ConsoleColors.RESET}",
                "${ConsoleColors.GREEN}Alive players: ${state.alivePlayers().map { it.id }} ${ConsoleColors.RESET}"
            )

            Language.RU -> listOf(
                "${ConsoleColors.MAGENTA}Игра закончена: ${state.finished} ${ConsoleColors.RESET}",
                "${ConsoleColors.MAGENTA}Текущий день: ${state.dayNumber} ${ConsoleColors.RESET}",
                "${ConsoleColors.MAGENTA}Текущая фаза: ${formatPhase(state.phase)} ${ConsoleColors.RESET}",
                "${ConsoleColors.GREEN}Победитель: ${formatWinner(state.winner)} ${ConsoleColors.RESET}",
                "${ConsoleColors.GREEN}Живые игроки: ${state.alivePlayers().map { it.id }} ${ConsoleColors.RESET}"
            )
        }
    }

    fun formatPhase(phase: Phase): String {
        return when (language) {
            Language.EN -> phase.name

            Language.RU -> when (phase) {
                Phase.DAY_DISCUSSION -> "дневное обсуждение"
                Phase.DAY_VOTING -> "дневное голосование"
                Phase.NIGHT_MAFIA -> "ночь мафии"
                Phase.NIGHT_GODFATHER -> "ночь дона"
                Phase.NIGHT_DOCTOR -> "ночь доктора"
                Phase.NIGHT_COMMISSAR -> "ночь комиссара"
                Phase.NIGHT_ESCORT -> "ночь проститутки"
                Phase.NIGHT_MANIAC -> "ночь маньяка"
                Phase.FINISHED -> "игра закончена"
            }
        }
    }

    private fun formatKilledEvent(event: GameEvent.PlayerKilled): String {
        return when (language) {
            Language.EN -> when (event.reason) {
                KillReason.DAY_VOTE -> "Player ${event.playerId} was voted out."
                KillReason.MAFIA_KILL -> "Player ${event.playerId} was killed at night."
                KillReason.MANIAC_KILL -> "Player ${event.playerId} was killed at night."
                KillReason.ESCORT_LINK -> "Player ${event.playerId} was killed at night."
                KillReason.DEBUG -> "Player ${event.playerId} was killed."
            }

            Language.RU -> when (event.reason) {
                KillReason.DAY_VOTE -> "Игрок ${event.playerId} выбыл по итогам голосования."
                KillReason.MAFIA_KILL -> "Игрок ${event.playerId} был убит ночью."
                KillReason.MANIAC_KILL -> "Игрок ${event.playerId} был убит ночью."
                KillReason.ESCORT_LINK -> "Игрок ${event.playerId} был убит ночью."
                KillReason.DEBUG -> "Игрок ${event.playerId} был убит."
            }
        }
    }

    private fun formatPhaseChange(
        from: Phase,
        to: Phase,
        dayNumber: Int
    ): String {
        return when (language) {
            Language.EN -> "Phase changed: ${formatPhase(from)} -> ${formatPhase(to)}, day $dayNumber."
            Language.RU -> "Фаза изменилась: ${formatPhase(from)} -> ${formatPhase(to)}, день $dayNumber."
        }
    }

    private fun formatWinner(winner: Winner?): String {
        return when (language) {
            Language.EN -> when (winner) {
                Winner.CIVILIANS -> "civilians"
                Winner.MAFIA -> "mafia"
                null -> "none"
            }

            Language.RU -> when (winner) {
                Winner.CIVILIANS -> "мирные жители"
                Winner.MAFIA -> "мафия"
                null -> "нет"
            }
        }
    }

    fun formatRoleReveal(state: GameState): List<String> {
        val lines = mutableListOf<String>()

        when (language) {
            Language.EN -> {
                lines.add("")
                lines.add("Final role reveal:")

                val mafiaPlayers = state.players
                    .filter { it.role == Role.MAFIA || it.role == Role.GODFATHER }
                    .sortedBy { it.id }

                if (mafiaPlayers.isEmpty()) {
                    lines.add("Mafia team: none")
                } else {
                    lines.add("Mafia team:")
                    mafiaPlayers.forEach { player ->
                        lines.add("  Player ${player.id}: ${formatRole(player.role)}, ${formatPlayerStatus(player)}")
                    }
                }

                lines.add("")
                lines.add("All players:")
                state.players.sortedBy { it.id }.forEach { player ->
                    lines.add("  Player ${player.id}: ${formatRole(player.role)}, ${formatPlayerStatus(player)}")
                }
            }

            Language.RU -> {
                lines.add("")
                lines.add("Итоговое раскрытие ролей:")

                val mafiaPlayers = state.players
                    .filter { it.role == Role.MAFIA || it.role == Role.GODFATHER }
                    .sortedBy { it.id }

                if (mafiaPlayers.isEmpty()) {
                    lines.add("Команда мафии: нет")
                } else {
                    lines.add("Команда мафии:")
                    mafiaPlayers.forEach { player ->
                        lines.add("  Игрок ${player.id}: ${formatRole(player.role)}, ${formatPlayerStatus(player)}")
                    }
                }

                lines.add("")
                lines.add("Все игроки:")
                state.players.sortedBy { it.id }.forEach { player ->
                    lines.add("  Игрок ${player.id}: ${formatRole(player.role)}, ${formatPlayerStatus(player)}")
                }
            }
        }

        return lines
    }

    private fun formatRole(role: Role): String {
        return when (language) {
            Language.EN -> role.name

            Language.RU -> when (role) {
                Role.CIVILIAN -> "мирный житель"
                Role.MAFIA -> "мафия"
                Role.GODFATHER -> "дон мафии"
                Role.COMMISSAR -> "комиссар"
                Role.DOCTOR -> "доктор"
                Role.MANIAC -> "маньяк"
                Role.ESCORT -> "проститутка"
            }
        }
    }

    private fun formatPlayerStatus(player: Player): String {
        return when (language) {
            Language.EN -> if (player.alive) {
                "alive"
            } else {
                "killed"
            }

            Language.RU -> if (player.alive) {
                "жив"
            } else {
                "убит"
            }
        }
    }
}