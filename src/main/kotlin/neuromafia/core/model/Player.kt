package neuromafia.core.model

data class Player(
    val id: Int,
    val name: String,
    val role: Role,
    val alive: Boolean = true
)