package com.example.varinbloknot.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.varinbloknot.R

object C {
    val Paper = Color(0xFFF6EFE0)
    val Ink = Color(0xFF3D2F27)
    val Ink2 = Color(0xFF6B5A4C)
    val Accent = Color(0xFF9C3A60)
    val AccentSoft = Color(0xFFF1DCE3)
    val Line = Color(0xFFE2D5BD)
    val Chip = Color(0xFFEFE3C9)
    val Card = Color(0xFFFFFAF0)
    val Photo = Color(0xFFFFFDF6)
    val Green = Color(0xFF2F6B3F)
    val GreenBg = Color(0xFFE3EDDC)
    val Red = Color(0xFFB3392F)
    val RedBg = Color(0xFFF6DDD8)
    val Amber = Color(0xFFD9A066)
    val Tape = Color(0x99E9C27C)
}

@OptIn(ExperimentalTextApi::class)
private fun nunito(w: Int) = Font(R.font.nunito, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))

@OptIn(ExperimentalTextApi::class)
val Caveat = FontFamily(
    Font(R.font.caveat, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.caveat, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)
val Nunito = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800))

/** Текст без material3: размер, цвет, шрифт. */
@Composable
fun T(
    text: String,
    size: TextUnit = 16.sp,
    color: Color = C.Ink,
    weight: Int = 400,
    caveat: Boolean = false,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            fontFamily = if (caveat) Caveat else Nunito,
            fontWeight = FontWeight(if (caveat && weight < 500) 500 else weight),
            fontSize = size,
            color = color,
            textAlign = align ?: TextAlign.Unspecified,
            lineHeight = lineHeight,
        ),
    )
}

fun money(n: Int): String = n.toString().reversed().chunked(3).joinToString(" ").reversed() + " ₽"
