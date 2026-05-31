package neuromafia.core.model

data class GameConfig(
    val mode: GameMode,
    val playerCount: Int,
    val mafiaCount: Int,
    val commissarEnabled: Boolean,
    val doctorEnabled: Boolean,
    val maniacEnabled: Boolean,
    val escortEnabled: Boolean,
    val provider: String,
    val model: String,
    val humanPlayerId: Int?
) {
    init {
        require(playerCount >= 4) {
            "Player count must be at least 4."
        }

        require(mafiaCount >= 1) {
            "Mafia count must be at least 1."
        }

        require(mafiaCount < playerCount) {
            "Mafia count must be less than player count."
        }

        val specialCivilianRoleCount =
            (if (commissarEnabled) 1 else 0) +
                    (if (doctorEnabled) 1 else 0) +
                    (if (escortEnabled) 1 else 0)

        val neutralRoleCount =
            if (maniacEnabled) 1 else 0

        val requiredPlayerCount = mafiaCount + specialCivilianRoleCount + neutralRoleCount

        require(requiredPlayerCount <= playerCount) {
            "Too many enabled roles for $playerCount players."
        }

        if (mode == GameMode.HUMAN) {
            require(humanPlayerId != null) {
                "Human player id must be specified in HUMAN mode."
            }

            require(humanPlayerId in 1..playerCount) {
                "Human player id must be between 1 and $playerCount."
            }
        }
    }

    fun describe(): String {
        return """
            Game config:
              mode: $mode
              players: $playerCount
              mafia: $mafiaCount
              commissar: $commissarEnabled
              doctor: $doctorEnabled
              maniac: $maniacEnabled
              escort: $escortEnabled
              provider: $provider
              model: $model
              human player id: ${humanPlayerId ?: "none"}
        """.trimIndent()
    }
}