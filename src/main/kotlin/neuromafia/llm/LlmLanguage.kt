package neuromafia.llm

// instructions for llm (also set ups a lang for it)
enum class LlmLanguage {
    EN,
    RU;

    fun instruction(): String {
        return when (this) {
            EN -> """
            Answer in English.
            All JSON string fields, including publicReasoning and speech, must be written in English.
        """.trimIndent()

            RU -> """
            Отвечай только на русском языке.
            Все строковые поля JSON, включая publicReasoning и speech, должны быть написаны на русском языке.
            Не используй английский язык в речи игрока.
        """.trimIndent()
        }
    }

    fun publicLanguageName(): String {
        return when (this) {
            EN -> "English"
            RU -> "Russian"
        }
    }

    companion object {
        fun fromCliValue(value: String): LlmLanguage {
            return when (value.lowercase()) {
                "en", "english" -> EN
                "ru", "russian" -> RU
                else -> error("Unsupported language: $value")
            }
        }
    }
}