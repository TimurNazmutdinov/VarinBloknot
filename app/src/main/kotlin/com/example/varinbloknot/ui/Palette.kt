package com.example.varinbloknot.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun parseHex(s: String): Color = Color(android.graphics.Color.parseColor(s))

/** Карандаш: корпус, деревянный кончик и грифель. Выбранный выдвинут вверх. */
@Composable
fun Pencil(color: Color, selected: Boolean, tall: Int, onClick: () -> Unit) {
    Box(Modifier.width(44.dp).height(96.dp).tap(onClick), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.width(28.dp).height(tall.dp).offset(y = if (selected) (-6).dp else 10.dp)) {
            val tip = 18.dp.toPx()
            drawRect(color, Offset(0f, tip), androidx.compose.ui.geometry.Size(size.width, size.height - tip))
            drawRect(Color(0x22000000), Offset(size.width * 0.7f, tip), androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height - tip))
            val wood = Path().apply { moveTo(0f, tip); lineTo(size.width / 2, 0f); lineTo(size.width, tip); close() }
            drawPath(wood, Color(0xFFE9CFA3))
            val lead = Path().apply { moveTo(size.width * 0.32f, tip * 0.45f); lineTo(size.width / 2, 0f); lineTo(size.width * 0.68f, tip * 0.45f); close() }
            drawPath(lead, color)
        }
    }
}

@Composable
fun PencilRow(colors: List<Color>, selected: Color?, modifier: Modifier = Modifier, onPick: (Color) -> Unit) {
    val heights = listOf(78, 86, 80, 84, 78, 88, 82, 84)
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFF6EBCF)).padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        colors.forEachIndexed { i, col -> Pencil(col, selected == col, heights[i % heights.size]) { onPick(col) } }
    }
}

@Composable
fun SmallChip(text: String, on: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(12.dp)).background(if (on) C.Accent else C.Chip).tap(onClick)
            .heightIn(min = 44.dp).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { T(text, 14.sp, if (on) Color.White else C.Ink, 800) }
}
