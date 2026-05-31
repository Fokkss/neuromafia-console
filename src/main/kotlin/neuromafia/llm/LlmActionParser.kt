package neuromafia.llm

import kotlinx.serialization.json.Json

class LlmActionParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(content: String): LlmActionResponse {
        val jsonText = extractJsonObject(content)

        return json.decodeFromString<LlmActionResponse>(jsonText)
    }

    private fun extractJsonObject(content: String): String {
        val start = content.indexOf('{')
        val end = content.lastIndexOf('}')

        require(start >= 0 && end >= start) {
            "LLM response does not contain JSON object: $content"
        }

        return content.substring(start, end + 1)
    }
}