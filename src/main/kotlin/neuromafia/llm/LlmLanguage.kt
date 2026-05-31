package neuromafia.llm

enum class LlmLanguage {
    EN,
    RU;

    fun instruction(): String {
        return when (this) {
            EN -> "Answer in English."
            RU -> "Отвечай на русском языке."
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