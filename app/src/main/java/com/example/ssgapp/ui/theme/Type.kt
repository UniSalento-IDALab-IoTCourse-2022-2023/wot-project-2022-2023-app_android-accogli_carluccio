package com.example.ssgapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ssgapp.R


val ChakraPetch = FontFamily(
    Font(R.font.chakrapetch_regular)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = ChakraPetch,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        //lineHeight = 24.sp,
        //letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ChakraPetch,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        //lineHeight = 28.sp,
        //letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ChakraPetch,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        //lineHeight = 16.sp,
        //letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ChakraPetch,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        //color = Color(0xFF6D6D6D)
    ),
    displayLarge = TextStyle(
        fontFamily = ChakraPetch,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    )
)