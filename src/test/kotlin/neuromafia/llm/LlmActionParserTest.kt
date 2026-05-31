package neuromafia.llm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LlmActionParserTest {
    @Test
    fun `parse should parse plain json`() {
        val parser = LlmActionParser()

        val parsed = parser.parse(
            """
                {
                  "publicReasoning": "Reason.",
                  "speech": "Speech.",
                  "targetId": 4,
                  "skip": false
                }
            """.trimIndent()
        )

        assertEquals("Reason.", parsed.publicReasoning)
        assertEquals("Speech.", parsed.speech)
        assertEquals(4, parsed.targetId)
        assertEquals(false, parsed.skip)
    }

    @Test
    fun `parse should extract json from surrounding text`() {
        val parser = LlmActionParser()

        val parsed = parser.parse(
            """
                Here is my move:
                {
                  "publicReasoning": "Reason.",
                  "speech": null,
                  "targetId": 2,
                  "skip": false
                }
            """.trimIndent()
        )

        assertEquals(2, parsed.targetId)
    }

    @Test
    fun `parse should fail when json is missing`() {
        val parser = LlmActionParser()

        assertFailsWith<IllegalArgumentException> {
            parser.parse("no json here")
        }
    }
}