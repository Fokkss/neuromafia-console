package neuromafia.core.model


sealed interface GameEvent {
    data class PlayerSpoke(
        val playerId: Int,
        val message: String
    ) : GameEvent

    data class PlayerNominated(
        val speakerId: Int,
        val nominatedPlayerId: Int
    ) : GameEvent

    data class PlayerVoted(
        val voterId: Int,
        val targetId: Int?
    ) : GameEvent

    data class DayVotingTie(
        val candidateIds: List<Int>
    ) : GameEvent

    data class PlayerKilled(
        val playerId: Int,
        val reason: KillReason
    ) : GameEvent

    data class WinnerDeclared(
        val winner: Winner
    ) : GameEvent
}