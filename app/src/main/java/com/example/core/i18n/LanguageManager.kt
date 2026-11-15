package com.example.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

@Composable
fun stringResource(key: TranslationKey): String {
    val language = LocalAppLanguage.current
    return Translations.get(key, language)
}

fun TranslationKey.resolve(language: AppLanguage): String {
    return Translations.get(this, language)
}
