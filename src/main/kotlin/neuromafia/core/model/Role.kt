package neuromafia.core.model

enum class Role(
    val team: Team
) {
    CIVILIAN(Team.CIVILIANS),
    MAFIA(Team.MAFIA),
    GODFATHER(Team.MAFIA),
    COMMISSAR(Team.CIVILIANS),
    DOCTOR(Team.CIVILIANS),
    MANIAC(Team.CIVILIANS), // my game relates on rules where maniac kills for the sake of civilians
    ESCORT(Team.CIVILIANS)
}