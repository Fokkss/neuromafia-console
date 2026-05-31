package neuromafia.llm

import kotlinx.serialization.Serializable

@Serializable
data class LlmActionResponse(
    val publicReasoning: String,
    val speech: String? = null,
    val targetId: Int? = null,
    val skip: Boolean = false
)