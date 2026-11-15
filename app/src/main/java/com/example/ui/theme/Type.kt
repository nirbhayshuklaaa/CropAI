package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val DisplayFontFamily = FontFamily(
    Font(R.font.poppins, FontWeight.Normal)
)

val BodyFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

val DevanagariFontFamily = FontFamily(
    Font(R.font.noto_sans_devanagari, FontWeight.Normal)
)

fun buildAppTypography(useDevanagariPrimary: Boolean = false): Typography {
    val headingFamily = if (useDevanagariPrimary) DevanagariFontFamily else DisplayFontFamily
    val bodyFamily = if (useDevanagariPrimary) DevanagariFontFamily else BodyFontFamily

    return Typography(
        displaySmall = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.25).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 34.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 30.sp
        ),
        titleLarge = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = headingFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 18.sp
        ),
        labelLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    )
}

val Typography = buildAppTypography(useDevanagariPrimary = false)
