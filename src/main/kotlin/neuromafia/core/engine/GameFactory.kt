package neuromafia.core.engine

import kotlin.random.Random
import neuromafia.core.model.GameConfig
import neuromafia.core.model.GameState
import neuromafia.core.model.Player
import neuromafia.core.model.Role

object GameFactory {
    fun create(
        config: GameConfig,
        random: Random = Random.Default
    ): GameState {
        val roles = buildRoles(config).shuffled(random)

        val players = roles.mapIndexed { index, role ->
            val id = index + 1

            Player(
                id = id,
                name = "Player $id",
                role = role
            )
        }

        return GameState(
            config = config,
            players = players
        )
    }

    private fun buildRoles(config: GameConfig): List<Role> {
        val roles = mutableListOf<Role>()

        roles.add(Role.GODFATHER)

        repeat(config.mafiaCount - 1) {
            roles.add(Role.MAFIA)
        }

        if (config.commissarEnabled) {
            roles.add(Role.COMMISSAR)
        }

        if (config.doctorEnabled) {
            roles.add(Role.DOCTOR)
        }

        if (config.maniacEnabled) {
            roles.add(Role.MANIAC)
        }

        if (config.escortEnabled) {
            roles.add(Role.ESCORT)
        }

        val civilianCount = config.playerCount - roles.size

        repeat(civilianCount) {
            roles.add(Role.CIVILIAN)
        }

        return roles
    }
}