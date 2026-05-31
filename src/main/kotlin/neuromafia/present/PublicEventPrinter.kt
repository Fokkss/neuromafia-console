package neuromafia.present

import neuromafia.core.model.GameEvent
import neuromafia.core.model.GameState
import neuromafia.core.model.Phase
import neuromafia.core.model.Winner
import neuromafia.msg.Language

class PublicEventPrinter(
    private val language: Language
) {
    fun printGameSummary(state: GameState) {
        println("")

        when (language) {
            Language.EN -> {
                println("Game finished: ${state.finished}")
                println("Current day: ${state.dayNumber}")
                println("Current phase: ${state.phase}")
                println("Winner: ${formatWinner(state.winner)}")
                println("Alive players: ${state.alivePlayers().map { it.id }}")
            }

            Language.RU -> {
                println("Игра закончена: ${state.finished}")
                println("Текущий день: ${state.dayNumber}")
                println("Текущая фаза: ${formatPhase(state.phase)}")
                println("Победитель: ${formatWinner(state.winner)}")
                println("Живые игроки: ${state.alivePlayers().map { it.id }}")
            }
        }
    }

    fun printPublicEvents(state: GameState) {
        println("")

        when (language) {
            Language.EN -> println("Public game log:")
            Language.RU -> println("Публичный лог игры:")
        }

        state.eventLog.forEach { event ->
            val line = publicLine(event)

            if (line != null) {
                println("  $line")
            }
        }
    }

    private fun publicLine(event: GameEvent): String? {
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
                    "Player ${event.voterId} voted."
                }

                Language.RU -> if (event.targetId == null) {
                    "Игрок ${event.voterId} пропустил голосование."
                } else {
                    "Игрок ${event.voterId} проголосовал."
                }
            }

            is GameEvent.DayVotingTie -> when (language) {
                Language.EN -> "Day voting ended with a tie."
                Language.RU -> "Дневное голосование закончилось ничьёй."
            }

            is GameEvent.PlayerKilled -> when (language) {
                Language.EN -> "A player was killed."
                Language.RU -> "Игрок был убит."
            }

            is GameEvent.PlayerMuted -> when (language) {
                Language.EN -> "A player cannot speak and vote today."
                Language.RU -> "Один из игроков сегодня не может говорить и голосовать."
            }

            is GameEvent.PhaseChanged ->
                phaseLine(event.from, event.to, event.dayNumber)

            is GameEvent.WinnerDeclared -> when (language) {
                Language.EN -> "Game finished. Winner: ${formatWinner(event.winner)}."
                Language.RU -> "Игра закончена. Победитель: ${formatWinner(event.winner)}."
            }

            is GameEvent.MafiaKillVoteRecorded ->
                null

            is GameEvent.MafiaKillTargetSelected ->
                null

            is GameEvent.MafiaKillTie ->
                null

            is GameEvent.GodfatherKillDecisionMade ->
                null

            is GameEvent.GodfatherCommissarChecked ->
                null

            is GameEvent.DoctorProtected ->
                null

            is GameEvent.CommissarChecked ->
                null

            is GameEvent.EscortVisited ->
                null

            is GameEvent.ManiacKillTargetSelected ->
                null
        }
    }

    private fun phaseLine(
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

    private fun formatPhase(phase: Phase): String {
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
}