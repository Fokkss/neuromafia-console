package neuromafia.core.action

sealed interface PlayerAction {
    data class DaySpeech(
        val playerId: Int,
        val message: String,
        val nominatedPlayerId: Int?
    ) : PlayerAction

    data class DayVote(
        val voterId: Int,
        val targetId: Int?
    ) : PlayerAction

    data class MafiaKillVote(
        val mafiaId: Int,
        val targetId: Int
    ) : PlayerAction

    data class GodfatherKillDecision(
        val godfatherId: Int,
        val targetId: Int
    ) : PlayerAction

    data class GodfatherCommissarCheck(
        val godfatherId: Int,
        val targetId: Int
    ) : PlayerAction

    data class CommissarCheck(
        val commissarId: Int,
        val targetId: Int
    ) : PlayerAction

    data class DoctorHeal(
        val doctorId: Int,
        val targetId: Int
    ) : PlayerAction

    data class ManiacKill(
        val maniacId: Int,
        val targetId: Int
    ) : PlayerAction

    data class EscortVisit(
        val escortId: Int,
        val targetId: Int
    ) : PlayerAction
}