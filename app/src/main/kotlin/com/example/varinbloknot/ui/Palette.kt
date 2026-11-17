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
    Box(Modifier.width(38.dp).height(96.dp).tap(onClick), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.width(26.dp).height(tall.dp).offset(y = if (selected) (-6).dp else 10.dp)) {
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

/** Ластик: розовый брусок с бумажной полоской. */
@Composable
fun EraserPencil(selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.width(38.dp).height(96.dp).tap(onClick), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.width(26.dp).height(52.dp).offset(y = if (selected) (-6).dp else 10.dp)) {
            drawRoundRect(Color(0xFFF2A8BD), Offset.Zero, androidx.compose.ui.geometry.Size(size.width, size.height), androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()))
            drawRect(Color(0xFFFFFFFF), Offset(0f, size.height * 0.55f), androidx.compose.ui.geometry.Size(size.width, size.height * 0.2f))
            drawRect(Color(0x22000000), Offset(size.width * 0.7f, 0f), androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height))
        }
    }
}

@Composable
fun PencilRow(
    colors: List<Color>, selected: Color?, modifier: Modifier = Modifier,
    eraserOn: Boolean = false, onEraser: (() -> Unit)? = null, onPick: (Color) -> Unit,
) {
    val heights = listOf(78, 86, 80, 84, 78, 88, 82, 84)
    Row(
        modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFF6EBCF)).padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        colors.forEachIndexed { i, col -> Pencil(col, selected == col && !eraserOn, heights[i % heights.size]) { onPick(col) } }
        if (onEraser != null) EraserPencil(eraserOn, onEraser)
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
