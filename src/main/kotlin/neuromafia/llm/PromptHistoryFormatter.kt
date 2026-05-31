package neuromafia.llm

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.KillReason
import neuromafia.core.model.Phase

class PromptHistoryFormatter {
    fun formatPublicHistory(
        state: GameState,
        language: LlmLanguage,
        maxEvents: Int = 40
    ): String {
        val visibleEvents = state.eventLog
            .mapNotNull { formatVisibleEvent(it, language) }
            .takeLast(maxEvents)

        if (visibleEvents.isEmpty()) {
            return when (language) {
                LlmLanguage.EN -> "No public events yet."
                LlmLanguage.RU -> "Публичных событий пока нет."
            }
        }

        return visibleEvents.joinToString(separator = "\n")
    }

    private fun formatVisibleEvent(
        event: GameEvent,
        language: LlmLanguage
    ): String? {
        return when (event) {
            is GameEvent.PlayerSpoke -> when (language) {
                LlmLanguage.EN -> "Player ${event.playerId} said: ${event.message}"
                LlmLanguage.RU -> "Игрок ${event.playerId} сказал: ${event.message}"
            }

            is GameEvent.PlayerNominated -> when (language) {
                LlmLanguage.EN -> "Player ${event.speakerId} nominated Player ${event.nominatedPlayerId}."
                LlmLanguage.RU -> "Игрок ${event.speakerId} выставил игрока ${event.nominatedPlayerId}."
            }

            is GameEvent.PlayerVoted -> when (language) {
                LlmLanguage.EN -> if (event.targetId == null) {
                    "Player ${event.voterId} skipped voting."
                } else {
                    "Player ${event.voterId} voted for Player ${event.targetId}."
                }

                LlmLanguage.RU -> if (event.targetId == null) {
                    "Игрок ${event.voterId} пропустил голосование."
                } else {
                    "Игрок ${event.voterId} проголосовал за игрока ${event.targetId}."
                }
            }

            is GameEvent.DayVotingTie -> when (language) {
                LlmLanguage.EN -> "Day voting tie between players ${event.candidateIds}."
                LlmLanguage.RU -> "Ничья в дневном голосовании между игроками ${event.candidateIds}."
            }

            is GameEvent.PlayerKilled -> formatKilledEvent(event, language)

            is GameEvent.PlayerMuted -> when (language) {
                LlmLanguage.EN -> "Player ${event.playerId} cannot speak or vote today."
                LlmLanguage.RU -> "Игрок ${event.playerId} сегодня не может говорить или голосовать."
            }

            is GameEvent.PhaseChanged -> when (language) {
                LlmLanguage.EN -> "Phase changed: ${event.from} -> ${event.to}, day ${event.dayNumber}."
                LlmLanguage.RU -> "Фаза изменилась: ${formatPhase(event.from)} -> ${formatPhase(event.to)}, день ${event.dayNumber}."
            }

            is GameEvent.WinnerDeclared -> when (language) {
                LlmLanguage.EN -> "Game finished. Winner: ${event.winner}."
                LlmLanguage.RU -> "Игра закончена. Победитель: ${event.winner}."
            }

            // Hidden/private night events.
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

    private fun formatKilledEvent(
        event: GameEvent.PlayerKilled,
        language: LlmLanguage
    ): String {
        return when (language) {
            LlmLanguage.EN -> when (event.reason) {
                KillReason.DAY_VOTE -> "Player ${event.playerId} was voted out. Role was not revealed."
                KillReason.MAFIA_KILL,
                KillReason.MANIAC_KILL,
                KillReason.ESCORT_LINK -> "Player ${event.playerId} was killed at night. Role was not revealed."
                KillReason.DEBUG -> "Player ${event.playerId} was killed. Role was not revealed."
            }

            LlmLanguage.RU -> when (event.reason) {
                KillReason.DAY_VOTE -> "Игрок ${event.playerId} выбыл по итогам голосования. Роль не раскрыта."
                KillReason.MAFIA_KILL,
                KillReason.MANIAC_KILL,
                KillReason.ESCORT_LINK -> "Игрок ${event.playerId} был убит ночью. Роль не раскрыта."
                KillReason.DEBUG -> "Игрок ${event.playerId} был убит. Роль не раскрыта."
            }
        }
    }

    private fun formatPhase(phase: Phase): String {
        return when (phase) {
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