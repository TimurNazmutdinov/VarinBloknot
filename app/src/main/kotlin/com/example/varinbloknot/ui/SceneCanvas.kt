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
import com.example.varinbloknot.data.CharDef
import com.example.varinbloknot.data.PaintObj
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.data.SceneArt
import com.example.varinbloknot.logic.nearestOpaque
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
    /** Точка на каждом раскрашиваемом предмете (в координатах кадра) — для значка-капли. */
    val anchors: Map<String, Offset>,
)

@Composable
fun rememberSceneAssets(scene: Scene): SceneAssets? {
    val c = LocalContent.current
    return produceState<SceneAssets?>(null, scene.id, c) {
        value = withContext(Dispatchers.IO) {
            val art = c.sceneArt(scene.dir) ?: return@withContext null
            val bmp = HashMap<String, Bitmap>()
            val img = HashMap<String, ImageBitmap>()
            fun load(n: String) {
                if (n in img) return
                val b = c.bitmap("${scene.dir}/layers/$n") ?: return
                img[n] = b.asImageBitmap(); bmp[n] = b
            }
            for (l in art.layers) { load(l.name); l.tint?.removeSuffix(".webp")?.let { load(it) } }
            scene.design.paint.forEach { p -> p.overlay?.let { load(it) } }
            val chars = c.characters()
            val ci = HashMap<String, ImageBitmap>()
            for (id in scene.stage.keys + scene.dialog.map { it.who }) {
                val d = chars[id] ?: continue
                c.bitmap("characters/" + d.file)?.let { ci[id] = it.asImageBitmap() }
            }
            val anchors = HashMap<String, Offset>()
            for (p in scene.design.paint) {
                val l = art.layers.firstOrNull { it.name == p.layer } ?: continue
                val b = bmp[p.overlay ?: p.layer] ?: continue
                // центр масс непрозрачных пикселей, затем ближайший к нему непрозрачный (у штор центр — на окне)
                var sx = 0L; var sy = 0L; var n = 0
                val step = maxOf(1, minOf(b.width, b.height) / 60)
                var y = 0
                while (y < b.height) {
                    var x = 0
                    while (x < b.width) { if (android.graphics.Color.alpha(b.getPixel(x, y)) > 128) { sx += x; sy += y; n++ }; x += step }
                    y += step
                }
                if (n == 0) continue
                val pt = nearestOpaque(b.width, b.height, (sx / n).toInt(), (sy / n).toInt()) { x, yy -> android.graphics.Color.alpha(b.getPixel(x, yy)) > 128 } ?: continue
                anchors[p.id] = Offset(l.x + pt.first * l.w / b.width.toFloat(), l.y + pt.second * l.h / b.height.toFloat())
            }
            SceneAssets(art, bmp, img, chars, ci, anchors)
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

private val NoHit = setOf("background", "ao", "shadows", "kshadows", "lampglow", "varya", "ceiling")

private fun alphaAt(a: SceneAssets, name: String, lx: Int, ly: Int, lw: Int, lh: Int, fx: Float, fy: Float): Boolean {
    val b = a.bmp[name] ?: return false
    val px = ((fx - lx) * b.width / lw).toInt().coerceIn(0, b.width - 1)
    val py = ((fy - ly) * b.height / lh).toInt().coerceIn(0, b.height - 1)
    return android.graphics.Color.alpha(b.getPixel(px, py)) > 40
}

/** Какой слой лежит под точкой кадра (сверху вниз; по непрозрачному пикселю). Силуэты тоже ловят нажатие. */
fun hitLayer(a: SceneAssets, hidden: Set<String>, ghosts: Set<String>, fx: Float, fy: Float): String? {
    for (l in a.art.layers.asReversed()) {
        if (l.name in NoHit) continue
        if (l.name in hidden && l.name !in ghosts) continue
        if (fx < l.x || fx >= l.x + l.w || fy < l.y || fy >= l.y + l.h) continue
        if (alphaAt(a, l.name, l.x, l.y, l.w, l.h, fx, fy)) return l.name
    }
    return null
}

/** Раскрашиваемый предмет в этой точке слоя (если на слое их несколько — тот, чья часть под пальцем). */
fun paintAt(a: SceneAssets, paint: List<PaintObj>, layer: String, fx: Float, fy: Float): PaintObj? {
    val cands = paint.filter { it.layer == layer }
    if (cands.isEmpty()) return null
    if (cands.size == 1) return cands[0]
    val l = a.art.layers.firstOrNull { it.name == layer } ?: return null
    return cands.firstOrNull { alphaAt(a, it.overlay ?: layer, l.x, l.y, l.w, l.h, fx, fy) }
}

private fun DrawScope.drawLayer(
    img: ImageBitmap, cam: Cam, x: Float, y: Float, w: Float, h: Float,
    alpha: Float = 1f, filter: ColorFilter? = null, scale: Float = 1f, pivotY: Float = 0.5f,
) {
    val sw = w * scale
    val sh = h * scale
    val ox = x + (w - sw) / 2
    val oy = y + (h - sh) * pivotY
    drawImage(
        img,
        dstOffset = IntOffset((cam.ox + ox * cam.s).roundToInt(), (cam.oy + oy * cam.s).roundToInt()),
        dstSize = IntSize(ceil(sw * cam.s).toInt(), ceil(sh * cam.s).toInt()),
        alpha = alpha, colorFilter = filter, filterQuality = FilterQuality.Medium,
    )
}

@Composable
fun SceneCanvas(
    a: SceneAssets,
    modifier: Modifier = Modifier,
    paint: List<PaintObj> = emptyList(),
    hidden: Set<String> = emptySet(),
    ghosts: Set<String> = emptySet(),
    selected: Set<String> = emptySet(),
    tints: Map<String, Color> = emptyMap(),
    fillAnim: Pair<String, Float>? = null,
    pop: Pair<Set<String>, Float>? = null,
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
                var lastTap = 0L
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
                        val now = System.currentTimeMillis()
                        val fxp = (start.x - c.ox) / c.s
                        val fyp = (start.y - c.oy) / c.s
                        if (interactive && now - lastTap < 300) {
                            // двойное касание: приблизить / вернуть
                            if (uz > 1.2f) uz = 1f else { uz = 2.2f; ux = fxp / a.art.width; uy = fyp / a.art.height }
                            lastTap = 0L
                        } else {
                            lastTap = now
                            tapState?.invoke(fxp, fyp)
                        }
                    }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            for (l in a.art.layers) {
                val x = l.x.toFloat(); val y = l.y.toFloat(); val w = l.w.toFloat(); val h = l.h.toFloat()
                if (l.name in ghosts) {
                    a.img[l.name]?.let { drawLayer(it, cam, x, y, w, h, filter = ColorFilter.tint(Color(0x66FFFFFF), BlendMode.SrcIn)) }
                    if (l.name in selected) a.img[l.name]?.let { drawLayer(it, cam, x, y, w, h, alpha = 0.9f, filter = ColorFilter.tint(C.Accent, BlendMode.SrcIn), scale = 1.07f) }
                    continue
                }
                if (l.name in hidden) continue
                val base = a.img[l.name] ?: continue
                val popP = pop?.takeIf { l.name in it.first }?.second
                val sc = if (popP != null) 0.8f + 0.2f * popP else 1f
                val al = popP ?: 1f
                if (l.name in selected) drawLayer(base, cam, x, y, w, h, alpha = 0.95f, filter = ColorFilter.tint(C.Accent, BlendMode.SrcIn), scale = 1.06f)

                val tintObj = paint.firstOrNull { it.mode == "tint" && it.layer == l.name && it.id in tints }
                if (tintObj != null) {
                    val tImg = a.img[l.tint?.removeSuffix(".webp")]
                    val col = tints[tintObj.id]
                    val t = fillAnim?.takeIf { it.first == tintObj.id }?.second ?: 1f
                    if (tImg != null && col != null) {
                        if (t < 1f) drawLayer(base, cam, x, y, w, h, alpha = al, scale = sc, pivotY = 1f)
                        drawLayer(tImg, cam, x, y, w, h, alpha = al * t, filter = ColorFilter.tint(col, BlendMode.Modulate), scale = sc, pivotY = 1f)
                    } else drawLayer(base, cam, x, y, w, h, alpha = al, scale = sc, pivotY = 1f)
                } else {
                    drawLayer(base, cam, x, y, w, h, alpha = al, scale = sc, pivotY = 1f)
                }
                for (p in paint) {
                    if (p.mode != "mask" || p.layer != l.name || p.id !in tints) continue
                    val oi = a.img[p.overlay ?: continue] ?: continue
                    val t = fillAnim?.takeIf { it.first == p.id }?.second ?: 1f
                    drawLayer(oi, cam, x, y, w, h, alpha = al * t, filter = ColorFilter.tint(tints[p.id]!!, BlendMode.Modulate), scale = sc, pivotY = 1f)
                }
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
