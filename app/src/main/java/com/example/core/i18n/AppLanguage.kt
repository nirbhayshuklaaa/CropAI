package com.example.core.i18n

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    BENGALI("bn", "Bengali", "বাংলা"),
    MARATHI("mr", "Marathi", "मराठी"),
    TELUGU("te", "Telugu", "తెలుగు"),
    TAMIL("ta", "Tamil", "தமிழ்");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
