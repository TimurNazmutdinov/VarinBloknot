package com.example.varinbloknot.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

class PaintStroke(val color: Color, val pts: List<Offset>, val width: Float)

/** Общее состояние раскраски: одни и те же штрихи видны и на странице, и в полноэкранном режиме. */
class PaintState {
    val strokes = mutableStateListOf<PaintStroke>()
    var color by mutableStateOf(Color(0xFFE9A0B8))
    var brush by mutableFloatStateOf(0.035f)
    fun undo() { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) }
}

fun parseHex(s: String): Color = Color(android.graphics.Color.parseColor(s))

private fun DrawScope.drawStrokes(list: List<PaintStroke>) {
    for (s in list) {
        if (s.pts.isEmpty()) continue
        val p = Path()
        val f = s.pts.first()
        p.moveTo(f.x * size.width, f.y * size.height)
        if (s.pts.size == 1) p.lineTo(f.x * size.width + 0.1f, f.y * size.height)
        for (i in 1 until s.pts.size) p.lineTo(s.pts[i].x * size.width, s.pts[i].y * size.height)
        drawPath(
            p, s.color.copy(alpha = 0.62f),
            style = Stroke(width = s.width * size.width, cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = BlendMode.Multiply,
        )
    }
}

/** Серый рисунок + штрихи. Один палец — красит (если включена «Раскраска»), два — приближают. */
@Composable
fun PaintSurface(gray: ImageBitmap, paint: PaintState, painting: Boolean, modifier: Modifier = Modifier, zoomable: Boolean = false) {
    var live by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    val paintingNow by rememberUpdatedState(painting)

    fun clamp(o: Offset, s: Float, w: Float, h: Float) = Offset(o.x.coerceIn(w * (1 - s), 0f), o.y.coerceIn(h * (1 - s), 0f))

    Box(
        modifier
            .clip(RoundedCornerShape(2.dp))
            .pointerInput(zoomable) {
                awaitEachGesture {
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    fun norm(p: Offset) = Offset(((p.x - pan.x) / scale) / w, ((p.y - pan.y) / scale) / h)
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var pts: MutableList<Offset>? = if (paintingNow) mutableListOf(norm(down.position)) else null
                    if (pts != null) { live = pts.toList(); down.consume() }
                    var multi = false
                    do {
                        val ev = awaitPointerEvent()
                        val pressed = ev.changes.filter { it.pressed }
                        if (pressed.size >= 2 && zoomable) {
                            multi = true; pts = null; live = emptyList()
                            val z = ev.calculateZoom()
                            val c = ev.calculateCentroid()
                            val ns = (scale * z).coerceIn(1f, 4f)
                            pan = clamp(c - (c - pan) * (ns / scale) + ev.calculatePan(), ns, w, h)
                            scale = ns
                            ev.changes.forEach { it.consume() }
                        } else if (pressed.size == 1 && !multi) {
                            val ch = pressed[0]
                            if (pts != null) {
                                pts.add(norm(ch.position)); live = pts.toList(); ch.consume()
                            } else if (zoomable && scale > 1f) {
                                pan = clamp(pan + ev.calculatePan(), scale, w, h); ch.consume()
                            }
                        }
                    } while (ev.changes.any { it.pressed })
                    val done = pts
                    if (done != null && !multi && done.isNotEmpty()) paint.strokes.add(PaintStroke(paint.color, done, paint.brush))
                    live = emptyList()
                }
            },
    ) {
        Canvas(
            Modifier.fillMaxSize().graphicsLayer(
                scaleX = scale, scaleY = scale, translationX = pan.x, translationY = pan.y,
                transformOrigin = TransformOrigin(0f, 0f),
            )
        ) {
            drawImage(gray, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
            drawStrokes(paint.strokes)
            if (live.isNotEmpty()) drawStrokes(listOf(PaintStroke(paint.color, live, paint.brush)))
        }
    }
}

/** Проявление цвета из серого штрихами: цветная картинка видна только там, где «прошёл карандаш». */
@Composable
fun RevealSurface(gray: ImageBitmap, color: ImageBitmap, paint: PaintState, progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        val dst = IntSize(size.width.toInt(), size.height.toInt())
        drawImage(gray, dstSize = dst)
        drawStrokes(paint.strokes)
        val n = 16
        val band = size.height / n
        drawIntoCanvas { c ->
            c.saveLayer(androidx.compose.ui.geometry.Rect(Offset.Zero, size), Paint())
            for (i in 0 until n) {
                val local = ((progress * (n + 4) - i) / 4f).coerceIn(0f, 1f)
                if (local <= 0f) continue
                val y = (i + 0.5f) * band
                val slant = band * 0.8f
                val xEnd = (size.width + 2 * band) * local - band
                drawLine(Color.Black, Offset(-band, y + slant), Offset(xEnd, y - slant), band * 2.1f, StrokeCap.Round)
            }
            drawImage(color, dstSize = dst, blendMode = BlendMode.SrcIn)
            c.restore()
        }
    }
}

/** Карандаш: корпус, деревянный кончик и грифель. Выбранный выдвинут вверх. */
@Composable
fun Pencil(color: Color, selected: Boolean, tall: Int, onClick: () -> Unit) {
    Box(Modifier.width(26.dp).height(100.dp).tap(onClick), contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.width(22.dp).height(tall.dp).offset(y = if (selected) (-8).dp else 8.dp)) {
            val tip = 16.dp.toPx()
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
fun PencilRow(colors: List<Color>, paint: PaintState, modifier: Modifier = Modifier) {
    val heights = listOf(84, 92, 86, 90, 84, 94, 88, 90)
    Row(
        modifier.background(Color(0xFFFBF2DC)).padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        colors.forEachIndexed { i, col ->
            Pencil(col, paint.color == col, heights[i % heights.size]) { paint.color = col }
        }
    }
}

@Composable
fun ModeToggle(painting: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.clip(RoundedCornerShape(16.dp)).background(C.Chip).padding(3.dp)) {
        listOf("Обычный" to false, "Раскраска" to true).forEach { (label, v) ->
            val on = painting == v
            Box(
                Modifier.clip(RoundedCornerShape(13.dp)).background(if (on) C.Accent else Color.Transparent)
                    .tap { onChange(v) }.padding(horizontal = 14.dp, vertical = 8.dp),
            ) { T(label, 14.sp, if (on) Color.White else C.Ink2, 800) }
        }
    }
}

/** Полноэкранная раскраска: щипок — приблизить, два пальца — двигать. */
@Composable
fun FullscreenColoring(
    gray: ImageBitmap, palette: List<Color>, paint: PaintState,
    painting: Boolean, onMode: (Boolean) -> Unit, onClose: () -> Unit,
) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().background(C.Photo).systemBarsPadding()) {
            PaintSurface(gray, paint, painting, Modifier.fillMaxSize().padding(bottom = 0.dp), zoomable = true)
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundButton(onClick = onClose) { T("✕", 18.sp, C.Ink, 800) }
                Spacer(Modifier.weight(1f))
                ModeToggle(painting, onMode)
                Spacer(Modifier.weight(1f))
                RoundButton(onClick = { paint.undo() }) { T("↶", 22.sp, C.Ink, 800) }
            }
            Column(
                Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Color(0xE6FBF6EA)).padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                T("Кисть", 11.sp, C.Ink2, 800)
                listOf(0.018f to 8, 0.035f to 16, 0.065f to 26).forEach { (w, d) ->
                    Box(
                        Modifier.size(34.dp).tap { paint.brush = w }
                            .then(if (paint.brush == w) Modifier.border(2.dp, C.Accent, CircleShape) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) { Box(Modifier.size(d.dp).clip(CircleShape).background(C.Ink)) }
                }
            }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp)
                    .clip(RoundedCornerShape(22.dp)).background(Color(0xF2FBF6EA)).padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    palette.forEach { col ->
                        Box(
                            Modifier.size(38.dp).clip(CircleShape).background(col)
                                .border(if (paint.color == col) 3.dp else 1.5.dp, if (paint.color == col) C.Ink else C.Line, CircleShape)
                                .tap { paint.color = col },
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                T("Щипок — приблизить, два пальца — двигать", 12.sp, C.Ink2, 600)
            }
        }
    }
}
