package neuromafia.core.model


sealed interface GameEvent {
    data class PhaseChanged(
        val from: Phase,
        val to: Phase,
        val dayNumber: Int
    ) : GameEvent

    data class MafiaKillVoteRecorded(
        val mafiaId: Int,
        val targetId: Int
    ) : GameEvent

    data class MafiaKillTargetSelected(
        val targetId: Int
    ) : GameEvent

    data class MafiaKillTie(
        val candidateIds: List<Int>
    ) : GameEvent

    data class GodfatherCommissarChecked(
        val godfatherId: Int,
        val targetId: Int,
        val isCommissar: Boolean
    ) : GameEvent

    data class GodfatherKillDecisionMade(
        val godfatherId: Int,
        val targetId: Int
    ) : GameEvent

    data class DoctorProtected(
        val doctorId: Int,
        val targetId: Int
    ) : GameEvent

    data class CommissarChecked(
        val commissarId: Int,
        val targetId: Int,
        val isMafia: Boolean
    ) : GameEvent

    data class EscortVisited(
        val escortId: Int,
        val targetId: Int
    ) : GameEvent

    data class PlayerMuted(
        val playerId: Int,
        val dayNumber: Int
    ) : GameEvent

    data class ManiacKillTargetSelected(
        val maniacId: Int,
        val targetId: Int
    ) : GameEvent

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