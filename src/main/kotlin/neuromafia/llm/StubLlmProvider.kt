package neuromafia.llm

class StubLlmProvider(
    private val response: String = """
        {
          "publicReasoning": "Stub reasoning.",
          "speech": "Stub speech.",
          "targetId": null,
          "skip": true
        }
    """.trimIndent()
) : LlmProvider {
    override suspend fun ask(request: LlmRequest): LlmResponse {
        return LlmResponse(
            content = response
        )
    }
}