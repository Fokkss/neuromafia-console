package neuromafia.llm

interface LlmProvider {
    suspend fun ask(request: LlmRequest): LlmResponse
}