package com.fasttimes.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fasttimes.R

@OptIn(ExperimentalTextApi::class)
val RobotoFlex = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(950), // Specify the full range of weights
            FontVariation.width(100f), // Specify the full range of widths
        )
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 1.sp
    ),
    headlineLargeEmphasized = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold ,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 2.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 1.sp
    ),
    headlineMediumEmphasized = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 2.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.sp
    ),
    // Keep the default font for body text for readability
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)