package neuromafia.present

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Phase
import neuromafia.core.model.Winner
import neuromafia.msg.Language

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

            // Hidden night/private events.
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
                "Game finished: ${state.finished}",
                "Current day: ${state.dayNumber}",
                "Current phase: ${formatPhase(state.phase)}",
                "Winner: ${formatWinner(state.winner)}",
                "Alive players: ${state.alivePlayers().map { it.id }}"
            )

            Language.RU -> listOf(
                "Игра закончена: ${state.finished}",
                "Текущий день: ${state.dayNumber}",
                "Текущая фаза: ${formatPhase(state.phase)}",
                "Победитель: ${formatWinner(state.winner)}",
                "Живые игроки: ${state.alivePlayers().map { it.id }}"
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
}