package neuromafia.llm.openrouter

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import neuromafia.llm.LlmLanguage
import neuromafia.llm.LlmRequest

class OpenRouterProviderTest {
    @Test
    fun `ask should return first choice message content`() = runBlocking {
        val engine = MockEngine { request ->
            assertTrue(request.url.toString().contains("/chat/completions"))
            assertEquals("Bearer test-key", request.headers[HttpHeaders.Authorization])

            respond(
                content = """
                    {
                      "choices": [
                        {
                          "message": {
                            "role": "assistant",
                            "content": "{\"publicReasoning\":\"ok\",\"targetId\":1,\"skip\":false}"
                          }
                        }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json"
                )
            )
        }

        val client = testClient(engine)

        val provider = OpenRouterProvider(
            apiKey = "test-key",
            model = "openai/gpt-oss-20b",
            client = client,
            baseUrl = "https://openrouter.ai/api/v1/chat/completions"
        )

        val response = provider.ask(
            LlmRequest(
                systemPrompt = "system",
                userPrompt = "user",
                language = LlmLanguage.EN
            )
        )

        assertEquals(
            "{\"publicReasoning\":\"ok\",\"targetId\":1,\"skip\":false}",
            response.content
        )

        client.close()
    }

    @Test
    fun `ask should fail when api key is blank`() = runBlocking {
        val client = testClient(
            MockEngine {
                respond("{}")
            }
        )

        val provider = OpenRouterProvider(
            apiKey = "",
            model = "openai/gpt-oss-20b",
            client = client
        )

        kotlin.test.assertFailsWith<IllegalArgumentException> {
            provider.ask(
                LlmRequest(
                    systemPrompt = "system",
                    userPrompt = "user",
                    language = LlmLanguage.EN
                )
            )
        }

        client.close()
    }

    private fun testClient(
        engine: MockEngine
    ): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }
        }
    }
}