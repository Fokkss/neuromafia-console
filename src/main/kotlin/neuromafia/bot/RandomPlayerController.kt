package neuromafia.bot

import kotlin.random.Random
import neuromafia.core.action.PlayerAction
import neuromafia.core.model.GameState
import neuromafia.core.model.Team

class RandomPlayerController(
    private val random: Random = Random.Default
) : PlayerController {
    override fun chooseDaySpeech(
        state: GameState,
        playerId: Int
    ): PlayerAction.DaySpeech {
        val candidates = state.alivePlayers()
            .filter { it.id != playerId }
            .filter { it.id !in state.nominatedPlayerIds }

        val nominatedPlayerId = if (candidates.isEmpty()) {
            null
        } else if (random.nextDouble() < 0.5) {
            candidates.random(random).id
        } else {
            null
        }

        return PlayerAction.DaySpeech(
            playerId = playerId,
            message = "player $playerId says something suspicious.",
            nominatedPlayerId = nominatedPlayerId
        )
    }

    override fun chooseDayVote(
        state: GameState,
        playerId: Int,
        nominatedPlayerIds: List<Int>
    ): PlayerAction.DayVote {
        val candidates = nominatedPlayerIds
            .map { state.playerById(it) }
            .filter { it.alive }

        val targetId = if (candidates.isEmpty()) {
            null
        } else if (random.nextDouble() < 0.15) {
            null
        } else {
            candidates.random(random).id
        }

        return PlayerAction.DayVote(
            voterId = playerId,
            targetId = targetId
        )
    }

    override fun chooseMafiaKillVote(
        state: GameState,
        playerId: Int
    ): PlayerAction.MafiaKillVote {
        val candidates = state.alivePlayers()
            .filter { it.role.team != Team.MAFIA }

        require(candidates.isNotEmpty()) {
            "no available mafia kill targets."
        }

        return PlayerAction.MafiaKillVote(
            mafiaId = playerId,
            targetId = candidates.random(random).id
        )
    }

    override fun chooseGodfatherKillDecision(
        state: GameState,
        playerId: Int,
        candidateIds: List<Int>
    ): PlayerAction.GodfatherKillDecision {
        require(candidateIds.isNotEmpty()) {
            "no candidates for godfather kill decision."
        }

        return PlayerAction.GodfatherKillDecision(
            godfatherId = playerId,
            targetId = candidateIds.random(random)
        )
    }

    override fun chooseGodfatherCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.GodfatherCommissarCheck {
        val candidates = state.alivePlayers()
            .filter { it.id != playerId }

        require(candidates.isNotEmpty()) {
            "no available godfather check targets."
        }

        return PlayerAction.GodfatherCommissarCheck(
            godfatherId = playerId,
            targetId = candidates.random(random).id
        )
    }

    override fun chooseCommissarCheck(
        state: GameState,
        playerId: Int
    ): PlayerAction.CommissarCheck {
        val candidates = state.alivePlayers()
            .filter { it.id != playerId }

        require(candidates.isNotEmpty()) {
            "no available commissar check targets."
        }

        return PlayerAction.CommissarCheck(
            commissarId = playerId,
            targetId = candidates.random(random).id
        )
    }

    override fun chooseDoctorHeal(
        state: GameState,
        playerId: Int
    ): PlayerAction.DoctorHeal {
        val candidates = state.alivePlayers()

        require(candidates.isNotEmpty()) {
            "no available doctor heal targets."
        }

        return PlayerAction.DoctorHeal(
            doctorId = playerId,
            targetId = candidates.random(random).id
        )
    }

    override fun chooseEscortVisit(
        state: GameState,
        playerId: Int
    ): PlayerAction.EscortVisit {
        val candidates = state.alivePlayers()
            .filter { it.id != playerId }

        require(candidates.isNotEmpty()) {
            "no available escort visit targets."
        }

        return PlayerAction.EscortVisit(
            escortId = playerId,
            targetId = candidates.random(random).id
        )
    }

    override fun chooseManiacKill(
        state: GameState,
        playerId: Int
    ): PlayerAction.ManiacKill {
        val candidates = state.alivePlayers()
            .filter { it.id != playerId }

        require(candidates.isNotEmpty()) {
            "no available maniac kill targets."
        }

        return PlayerAction.ManiacKill(
            maniacId = playerId,
            targetId = candidates.random(random).id
        )
    }
}