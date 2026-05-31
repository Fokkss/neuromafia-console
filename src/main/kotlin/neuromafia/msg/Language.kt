package neuromafia.msg

enum class Language {
    EN,
    RU;

    companion object {
        fun fromCliValue(value: String): Language {
            return when (value.lowercase()) {
                "en", "english" -> EN
                "ru", "russian" -> RU
                else -> error("unsupported language: $value")
            }
        }
    }
}