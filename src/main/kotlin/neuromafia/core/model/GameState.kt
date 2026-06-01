package neuromafia.core.model

import kotlin.collections.filter

data class GameState(
    val config: GameConfig,
    val players: List<Player>,
    val phase: Phase = Phase.DAY_DISCUSSION,
    val dayNumber: Int = 1,
    val winner: Winner? = null,
    val nominatedPlayerIds: List<Int> = emptyList(),
    val eventLog: List<GameEvent> = emptyList(),
    val pendingMafiaKillTargetId: Int? = null,
    val pendingMafiaKillCandidateIds: List<Int> = emptyList(),
    val godfatherCommissarChecks: Map<Int, Boolean> = emptyMap(),
    val protectedPlayerId: Int? = null,
    val escortVisitedPlayerId: Int? = null,
    val mutedPlayerIds: Set<Int> = emptySet(),
    val pendingManiacKillTargetId: Int? = null,
    val commissarChecks: Map<Int, Boolean> = emptyMap(),
) {
    val finished: Boolean get() = winner != null || phase == Phase.FINISHED

    fun alivePlayers(): List<Player> {
        return players.filter { it.alive }
    }

    fun aliveMafiaPlayers(): List<Player> {
        return alivePlayers().filter { it.role.team == Team.MAFIA }
    }

    fun aliveCivilianTeamPlayers(): List<Player> {
        return alivePlayers().filter { it.role.team == Team.CIVILIANS }
    }

    fun aliveGodfather(): Player? {
        return alivePlayers().firstOrNull { it.role == Role.GODFATHER }
    }

    fun aliveDoctor(): Player? {
        return alivePlayers().firstOrNull { it.role == Role.DOCTOR }
    }

    fun aliveCommissar(): Player? {
        return alivePlayers().firstOrNull { it.role == Role.COMMISSAR }
    }

    fun aliveEscort(): Player? {
        return alivePlayers().firstOrNull { it.role == Role.ESCORT }
    }

    fun aliveManiac(): Player? {
        return alivePlayers().firstOrNull { it.role == Role.MANIAC }
    }

    fun playerById(id: Int): Player {
        return players.firstOrNull { it.id == id }
            ?: error("player with id $id not found.")
    }
}