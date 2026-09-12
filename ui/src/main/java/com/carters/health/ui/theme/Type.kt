package com.carters.health.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Two families only: serif for display/headline, sans for the rest. */
val HealthTypography = Typography(
    displayLarge = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontSize = 64.sp, lineHeight = 66.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontSize = 48.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontSize = 38.sp, lineHeight = 42.sp),
    headlineLarge = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = SerifFamily, fontWeight = FontWeight.Normal, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = SansFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp),
)
