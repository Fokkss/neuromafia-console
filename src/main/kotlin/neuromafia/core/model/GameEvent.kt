package neuromafia.core.model


sealed interface GameEvent {
    data class PlayerKilled(
        val playerId: Int,
        val reason: KillReason
    ) : GameEvent

    data class PlayerNominated(
        val speakerId: Int,
        val nominatedPlayerId: Int
    ) : GameEvent

    data class WinnerDeclared(
        val winner: Winner
    ) : GameEvent
}