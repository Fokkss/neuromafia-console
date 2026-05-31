package neuromafia.llm.openrouter

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import neuromafia.llm.LlmProvider
import neuromafia.llm.LlmRequest
import neuromafia.llm.LlmResponse

class OpenRouterProvider(
    private val apiKey: String,
    private val model: String,
    private val client: HttpClient,
    private val baseUrl: String = "https://openrouter.ai/api/v1/chat/completions"
) : LlmProvider {
    override suspend fun ask(request: LlmRequest): LlmResponse {
        require(apiKey.isNotBlank()) {
            "OpenRouter API key is blank."
        }

        val openRouterRequest = OpenRouterChatRequest(
            model = model,
            messages = listOf(
                OpenRouterMessage(
                    role = "system",
                    content = request.systemPrompt
                ),
                OpenRouterMessage(
                    role = "user",
                    content = request.userPrompt
                )
            )
        )

        try {
            val response = client.post(baseUrl) {
                bearerAuth(apiKey)
                contentType(ContentType.Application.Json)

                // Optional OpenRouter headers. Helpful if later you want app attribution.
                header("HTTP-Referer", "https://github.com")
                header("X-Title", "Neuromafia")

                setBody(openRouterRequest)
            }.body<OpenRouterChatResponse>()

            val content = response.choices.firstOrNull()?.message?.content
                ?: error("OpenRouter response does not contain choices[0].message.content.")

            return LlmResponse(content = content)
        } catch (exception: ClientRequestException) {
            throw IllegalStateException(
                "OpenRouter client error: ${exception.response.status}",
                exception
            )
        } catch (exception: ServerResponseException) {
            throw IllegalStateException(
                "OpenRouter server error: ${exception.response.status}",
                exception
            )
        }
    }

    companion object {
        fun apiKeyFromEnvironment(): String {
            return System.getenv("OPENROUTER_API_KEY")
                ?: error("OPENROUTER_API_KEY environment variable is not set.")
        }
    }
}