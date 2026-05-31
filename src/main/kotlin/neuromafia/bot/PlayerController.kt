package neuromafia.bot

import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameState

interface PlayerController {
    fun chooseDaySpeech(
        state: GameState,
        playerId: Int
    ): PlayerAction.DaySpeech

    fun chooseDayVote(
        state: GameState,
        playerId: Int,
        nominatedPlayerIds: List<Int>
    ): PlayerAction.DayVote

    fun chooseMafiaKillVote(
        state: GameState,
        playerId: Int
    ): PlayerAction.MafiaKillVote

    fun chooseGodfatherKillDecision(
        state: GameState,
        playerId: Int,
        candidateIds: List<Int>
    ): PlayerAction.GodfatherKillDecision

    fun chooseGodfatherCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.GodfatherCommissarCheck

    fun chooseCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.CommissarCheck

    fun chooseDoctorHeal(
        state: GameState,
        playerId: Int
    ): PlayerAction.DoctorHeal

    fun chooseManiacKill(
        state: GameState,
        playerId: Int
    ): PlayerAction.ManiacKill

    fun chooseEscortVisit(
        state: GameState,
        playerId: Int
    ): PlayerAction.EscortVisit
}


// realizations
class ScriptedPlayerController(
    private val daySpeeches: MutableList<PlayerAction.DaySpeech> = mutableListOf(),
    private val dayVotes: MutableList<PlayerAction.DayVote> = mutableListOf(),
    private val mafiaKillVotes: MutableList<PlayerAction.MafiaKillVote> = mutableListOf(),
    private val godfatherKillDecisions: MutableList<PlayerAction.GodfatherKillDecision> = mutableListOf(),
    private val godfatherCommissarChecks: MutableList<PlayerAction.GodfatherCommissarCheck> = mutableListOf(),
    private val commissarChecks: MutableList<PlayerAction.CommissarCheck> = mutableListOf(),
    private val doctorHeals: MutableList<PlayerAction.DoctorHeal> = mutableListOf(),
    private val escortVisits: MutableList<PlayerAction.EscortVisit> = mutableListOf(),
    private val maniacKills: MutableList<PlayerAction.ManiacKill> = mutableListOf()
) : PlayerController {
    override fun chooseDaySpeech(
        state: GameState,
        playerId: Int
    ): PlayerAction.DaySpeech {
        return daySpeeches.removeFirst()
    }

    override fun chooseDayVote(
        state: GameState,
        playerId: Int,
        nominatedPlayerIds: List<Int>
    ): PlayerAction.DayVote {
        return dayVotes.removeFirst()
    }

    override fun chooseMafiaKillVote(
        state: GameState,
        playerId: Int
    ): PlayerAction.MafiaKillVote {
        return mafiaKillVotes.removeFirst()
    }

    override fun chooseGodfatherKillDecision(
        state: GameState,
        playerId: Int,
        candidateIds: List<Int>
    ): PlayerAction.GodfatherKillDecision {
        return godfatherKillDecisions.removeFirst()
    }

    override fun chooseGodfatherCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.GodfatherCommissarCheck {
        return godfatherCommissarChecks.removeFirst()
    }

    override fun chooseCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.CommissarCheck {
        return commissarChecks.removeFirst()
    }

    override fun chooseDoctorHeal(
        state: GameState,
        playerId: Int
    ): PlayerAction.DoctorHeal {
        return doctorHeals.removeFirst()
    }

    override fun chooseEscortVisit(
        state: GameState,
        playerId: Int
    ): PlayerAction.EscortVisit {
        return escortVisits.removeFirst()
    }

    override fun chooseManiacKill(
        state: GameState,
        playerId: Int
    ): PlayerAction.ManiacKill {
        return maniacKills.removeFirst()
    }
}