package neuromafia.core.model

enum class Role(
    val team: Team
) {
    CIVILIAN(Team.CIVILIANS),
    MAFIA(Team.MAFIA),
    GODFATHER(Team.MAFIA),
    COMMISSAR(Team.CIVILIANS),
    DOCTOR(Team.CIVILIANS),
    MANIAC(Team.CIVILIANS),
    ESCORT(Team.CIVILIANS)
}