package com.example.varinbloknot.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.varinbloknot.data.CharDef
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.data.SceneArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Слои сцены и герои, загруженные один раз и державшиеся в памяти, пока сцена открыта. */
class SceneAssets(
    val art: SceneArt,
    val bmp: Map<String, Bitmap>,
    val img: Map<String, ImageBitmap>,
    val chars: Map<String, CharDef>,
    val charImg: Map<String, ImageBitmap>,
)

@Composable
fun rememberSceneAssets(scene: Scene): SceneAssets? {
    val c = LocalContent.current
    return produceState<SceneAssets?>(null, scene.id, c) {
        value = withContext(Dispatchers.IO) {
            val art = c.sceneArt(scene.dir) ?: return@withContext null
            val bmp = HashMap<String, Bitmap>()
            val img = HashMap<String, ImageBitmap>()
            for (l in art.layers) {
                for (n in listOfNotNull(l.name, l.tint?.removeSuffix(".webp"))) {
                    if (n in img) continue
                    val b = c.bitmap("${scene.dir}/layers/$n") ?: continue
                    img[n] = b.asImageBitmap()
                    if (n == l.name) bmp[n] = b
                }
            }
            val chars = c.characters()
            val ids = scene.stage.keys + scene.dialog.map { it.who }
            val ci = HashMap<String, ImageBitmap>()
            for (id in ids) {
                val d = chars[id] ?: continue
                c.bitmap("characters/" + d.file)?.let { ci[id] = it.asImageBitmap() }
            }
            SceneAssets(art, bmp, img, chars, ci)
        }
    }.value
}

class Cam(val s: Float, val ox: Float, val oy: Float)

/** Камера: Crop на весь контейнер + зум + фокус (в долях кадра). */
fun camFor(vw: Float, vh: Float, aw: Int, ah: Int, zoom: Float, fx: Float, fy: Float): Cam {
    val s = maxOf(vw / aw, vh / ah) * zoom
    val cw = aw * s
    val ch = ah * s
    val ox = (vw / 2 - fx * cw).coerceIn(minOf(0f, vw - cw), 0f)
    val oy = (vh / 2 - fy * ch).coerceIn(minOf(0f, vh - ch), 0f)
    return Cam(s, ox, oy)
}

class Placed(val image: ImageBitmap, val footX: Float, val footY: Float, val heightPx: Float)

private val NoHit = setOf("background", "ao", "shadows", "kshadows", "lampglow", "varya")

/** Какой слой лежит под точкой кадра (сверху вниз; по непрозрачному пикселю). */
fun hitLayer(a: SceneAssets, hidden: Set<String>, fx: Float, fy: Float): String? {
    for (l in a.art.layers.asReversed()) {
        if (l.name in hidden || l.name in NoHit) continue
        if (fx < l.x || fx >= l.x + l.w || fy < l.y || fy >= l.y + l.h) continue
        val b = a.bmp[l.name] ?: continue
        val px = ((fx - l.x) * b.width / l.w).toInt().coerceIn(0, b.width - 1)
        val py = ((fy - l.y) * b.height / l.h).toInt().coerceIn(0, b.height - 1)
        if (android.graphics.Color.alpha(b.getPixel(px, py)) > 40) return l.name
    }
    return null
}

@Composable
fun SceneCanvas(
    a: SceneAssets,
    modifier: Modifier = Modifier,
    hidden: Set<String> = emptySet(),
    tints: Map<String, Color> = emptyMap(),
    zoom: Float = 1f, fx: Float = 0.5f, fy: Float = 0.5f,
    placed: List<Placed> = emptyList(),
    interactive: Boolean = false,
    onTap: ((frameX: Float, frameY: Float) -> Unit)? = null,
    drawOver: (DrawScope.(Cam) -> Unit)? = null,
    overlay: @Composable BoxScope.(Cam) -> Unit = {},
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var uz by remember { mutableFloatStateOf(1f) }
    var ux by remember(fx) { mutableFloatStateOf(fx) }
    var uy by remember(fy) { mutableFloatStateOf(fy) }
    val z = if (interactive) zoom * uz else zoom
    val cx = if (interactive) ux else fx
    val cy = if (interactive) uy else fy
    val cam = if (size.width == 0) Cam(1f, 0f, 0f) else camFor(size.width.toFloat(), size.height.toFloat(), a.art.width, a.art.height, z, cx, cy)
    val camState by rememberUpdatedState(cam)
    val tapState by rememberUpdatedState(onTap)

    Box(
        modifier.clipToBounds()
            .onSizeChanged { size = it }
            .pointerInput(interactive) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val start = down.position
                    var moved = false
                    do {
                        val ev = awaitPointerEvent()
                        val pressed = ev.changes.filter { it.pressed }
                        if (interactive && (pressed.size >= 2 || (pressed.size == 1 && (pressed[0].position - start).getDistance() > viewConfiguration.touchSlop))) {
                            moved = true
                            val zz = ev.calculateZoom()
                            val pan = ev.calculatePan()
                            uz = (uz * zz).coerceIn(1f, 3f)
                            val c = camState
                            ux -= pan.x / (a.art.width * c.s)
                            uy -= pan.y / (a.art.height * c.s)
                            ev.changes.forEach { it.consume() }
                        }
                    } while (ev.changes.any { it.pressed })
                    if (!moved) {
                        val c = camState
                        tapState?.invoke((start.x - c.ox) / c.s, (start.y - c.oy) / c.s)
                    }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            for (l in a.art.layers) {
                if (l.name in hidden) continue
                val t = tints[l.name]
                val image = if (t != null && l.tint != null) a.img[l.tint.removeSuffix(".webp")] else a.img[l.name]
                if (image == null) continue
                drawImage(
                    image,
                    dstOffset = IntOffset((cam.ox + l.x * cam.s).roundToInt(), (cam.oy + l.y * cam.s).roundToInt()),
                    dstSize = IntSize(ceil(l.w * cam.s).toInt(), ceil(l.h * cam.s).toInt()),
                    colorFilter = if (t != null && l.tint != null) ColorFilter.tint(t, BlendMode.Modulate) else null,
                    filterQuality = FilterQuality.Medium,
                )
            }
            for (p in placed) {
                val h = p.heightPx * cam.s
                val w = h * p.image.width / p.image.height
                drawImage(
                    p.image,
                    dstOffset = IntOffset((cam.ox + p.footX * cam.s - w / 2).roundToInt(), (cam.oy + p.footY * cam.s - h).roundToInt()),
                    dstSize = IntSize(ceil(w).toInt(), ceil(h).toInt()),
                    filterQuality = FilterQuality.Medium,
                )
            }
            drawOver?.invoke(this, cam)
        }
        overlay(cam)
    }
}
