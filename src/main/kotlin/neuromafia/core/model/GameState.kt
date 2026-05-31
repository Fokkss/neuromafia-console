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
) {
    val finished: Boolean
        get() = winner != null || phase == Phase.FINISHED

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

    fun playerById(id: Int): Player {
        return players.firstOrNull { it.id == id }
            ?: error("Player with id $id not found.")
    }
}