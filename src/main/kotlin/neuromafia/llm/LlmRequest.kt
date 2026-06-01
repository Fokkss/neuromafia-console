package neuromafia.llm

data class LlmRequest(
    val systemPrompt: String,
    val userPrompt: String,
    val language: LlmLanguage
)