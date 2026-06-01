package neuromafia.core.engine

sealed interface DayVoting {
    data object NoCandidates : DayVoting

    data class Killed(
        val playerId: Int
    ) : DayVoting

    data class Tie(
        val candidateIds: List<Int>
    ) : DayVoting
}