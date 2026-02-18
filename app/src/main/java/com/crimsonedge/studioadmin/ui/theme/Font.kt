package com.crimsonedge.studioadmin.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.crimsonedge.studioadmin.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val fontName = GoogleFont("Plus Jakarta Sans")

val AppFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.Light),
    Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = fontName, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)
