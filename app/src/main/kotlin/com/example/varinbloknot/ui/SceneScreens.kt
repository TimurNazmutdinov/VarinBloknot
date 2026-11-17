package com.example.varinbloknot.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.varinbloknot.data.DesignItem
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.data.Speaker
import kotlin.math.roundToInt

/** Экран шага: прокручиваемое содержимое + кнопка внизу. */
@Composable
fun StepLayout(bottom: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = content)
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) { bottom() }
    }
}

/** Что игрок сделал на шаге «Оформление»: купленные предметы и выбранные цвета. */
class DesignState {
    val bought = mutableStateListOf<String>()
    val tints = mutableStateMapOf<String, Color>()
}

private fun Scene.needsBuying(it: DesignItem, st: DesignState) =
    design.budget != null && it.price != null && it.name !in st.bought

/** Слои, которые пока скрыты (предметы с ценой, ещё не купленные). */
fun Scene.hiddenLayers(st: DesignState): Set<String> =
    design.items.filter { needsBuying(it, st) }.flatMap { it.layers }.toSet()

fun Scene.spent(st: DesignState) = design.items.filter { it.name in st.bought }.sumOf { it.price ?: 0 }

private fun joinRu(a: List<String>) = when (a.size) {
    0 -> ""
    1 -> a[0]
    else -> a.dropLast(1).joinToString(", ") + " и " + a.last()
}

/** Подставляет в реплику то, что выбрал игрок. */
fun Scene.fill(text: String, st: DesignState): String {
    val boughtItems = design.items.filter { it.name in st.bought }
    val left = (design.budget ?: 0) - spent(st)
    return text
        .replace("{bought}", if (boughtItems.isEmpty()) "пока ничего" else joinRu(boughtItems.map { it.acc }))
        .replace("{left}", money(left))
        .replace("{spent}", money(spent(st)))
}

@Composable
fun SceneScreen(
    scene: Scene, speakers: Map<String, Speaker>, hasNext: Boolean,
    onExit: () -> Unit, onNotebook: () -> Unit,
    onSaveCard: () -> Unit, onFinish: (next: Boolean) -> Unit,
) {
    var step by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    val state = remember(scene.id) { DesignState() }
    val assets = rememberSceneAssets(scene)
    androidx.activity.compose.BackHandler { if (step == 0) onExit() else step-- }

    Column(Modifier.fillMaxSize().background(C.Paper).systemBarsPadding()) {
        TopBar("Сцена ${scene.id} · ${StepNames[step]}", step, onBack = { if (step == 0) onExit() else step-- }, onNotebook = onNotebook)
        Box(Modifier.weight(1f)) {
            when (step) {
                0 -> IntroStep(scene) { step = 1 }
                1 -> if (assets != null) DesignStep(scene, assets, state) { step = 2 } else LoadingBox()
                2 -> if (assets != null) LiveStep(scene, assets, speakers, state) { step = 3 } else LoadingBox()
                3 -> TaskStep(scene.task, state.bought.toSet()) { step = 4 }
                else -> CardStep(scene, hasNext, onSaveCard, onFinish)
            }
        }
    }
}

@Composable
private fun LoadingBox() = Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { T("Загружаю сцену…", 16.sp, C.Ink2, 700) }

@Composable
private fun IntroStep(scene: Scene, onStart: () -> Unit) {
    StepLayout(bottom = { PrimaryButton("Начать", onClick = onStart) }) {
        Polaroid(Modifier.padding(top = 6.dp)) { ArtImage(scene.color, Modifier.fillMaxWidth().aspectRatio(1.6f)) }
        Column(Modifier.padding(horizontal = 22.dp, vertical = 6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { scene.tags.forEach { Chip(it) } }
            Spacer(Modifier.height(8.dp))
            T(scene.title, 46.sp, C.Ink, 700, caveat = true, lineHeight = 48.sp)
            Spacer(Modifier.height(6.dp))
            T(scene.story, 17.sp, C.Ink, 400, lineHeight = 26.sp)
        }
    }
}

private class Sel(val key: String, val label: String, val item: DesignItem?, val tintLayers: List<String>)

@Composable
private fun DesignStep(scene: Scene, a: SceneAssets, st: DesignState, onDone: () -> Unit) {
    val d = scene.design
    val palette = remember(scene.id) { d.palette.map { parseHex(it) } }
    var sel by remember { mutableStateOf<Sel?>(null) }
    var warn by remember { mutableStateOf(false) }
    val hidden = scene.hiddenLayers(st)
    val spent = scene.spent(st)
    val tintable = remember(a) { a.art.layers.filter { it.tint != null }.map { it.name } }

    fun selFor(it: DesignItem) = Sel(it.name, it.name, it, it.layers.filter { l -> l in tintable })
    fun objectFor(layer: String): Sel? {
        d.items.firstOrNull { layer in it.layers }?.let { return selFor(it) }
        if (layer in tintable) return Sel(layer, d.layerNames[layer] ?: layer, null, listOf(layer))
        return null
    }
    fun rectOf(layers: List<String>): androidx.compose.ui.geometry.Rect? {
        val ls = a.art.layers.filter { it.name in layers }
        if (ls.isEmpty()) return null
        return androidx.compose.ui.geometry.Rect(
            ls.minOf { it.x }.toFloat(), ls.minOf { it.y }.toFloat(),
            ls.maxOf { it.x + it.w }.toFloat(), ls.maxOf { it.y + it.h }.toFloat(),
        )
    }

    val coloredCount = tintable.count { it !in hidden && it in st.tints }
    val tintableCount = tintable.count { it !in hidden }

    StepLayout(bottom = { PrimaryButton("Готово, оживить сцену", onClick = onDone) }) {
        // Рисунок крупно: тап по предмету, щипок — приблизить, палец — двигать
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            SceneCanvas(
                a, Modifier.fillMaxSize(), hidden = hidden, tints = st.tints,
                interactive = true,
                onTap = { fx, fy ->
                    val hit = hitLayer(a, hidden, fx, fy)
                    var s = hit?.let { objectFor(it) }
                    if (s == null) {
                        // пунктир на месте некупленного предмета
                        s = d.items.filter { it.layers.isNotEmpty() && scene.needsBuying(it, st) }
                            .firstOrNull { rectOf(it.layers)?.contains(Offset(fx, fy)) == true }
                            ?.let { selFor(it) }
                    }
                    sel = s; warn = false
                },
                drawOver = { cam ->
                    val dash = PathEffect.dashPathEffect(floatArrayOf(18f, 12f))
                    for (item in d.items) {
                        if (!scene.needsBuying(item, st)) continue
                        val r = rectOf(item.layers) ?: continue
                        val tl = Offset(cam.ox + r.left * cam.s, cam.oy + r.top * cam.s)
                        drawRoundRect(Color(0x66FFFDF6), tl, Size(r.width * cam.s, r.height * cam.s), CornerRadius(10f))
                        drawRoundRect(C.Accent, tl, Size(r.width * cam.s, r.height * cam.s), CornerRadius(10f), style = Stroke(4f, pathEffect = dash))
                    }
                    sel?.let { s ->
                        val ls = if (s.item != null) s.item.layers else s.tintLayers
                        val r = rectOf(ls.filter { it !in hidden }.ifEmpty { ls })
                        if (r != null) drawRoundRect(
                            C.Accent, Offset(cam.ox + r.left * cam.s, cam.oy + r.top * cam.s),
                            Size(r.width * cam.s, r.height * cam.s), CornerRadius(10f), style = Stroke(6f),
                        )
                    }
                },
            ) { cam ->
                for (item in d.items) {
                    if (!scene.needsBuying(item, st)) continue
                    val r = rectOf(item.layers) ?: continue
                    Box(
                        Modifier.offset { IntOffset((cam.ox + r.left * cam.s).roundToInt(), (cam.oy + r.top * cam.s).roundToInt() - 14.dp.roundToPx()) }
                            .clip(RoundedCornerShape(8.dp)).background(C.Accent).padding(horizontal = 7.dp, vertical = 2.dp),
                    ) { T("＋ " + item.name + " · " + money(item.price ?: 0), 11.sp, Color.White, 800) }
                }
            }
            Box(
                Modifier.align(Alignment.BottomStart).padding(8.dp).clip(RoundedCornerShape(12.dp))
                    .background(Color(0xEEFFFAF0)).padding(horizontal = 10.dp, vertical = 6.dp),
            ) { T(if (sel == null) d.hint else "Выбрано: " + sel!!.label, 12.sp, C.Ink, 800) }
        }

        if (d.budget != null) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    T("Бюджет ${money(d.budget)}", 14.sp, C.Ink, 800)
                    T("осталось ${money(d.budget - spent)}", 14.sp, C.Accent, 800)
                }
                Box(Modifier.padding(top = 4.dp).fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(C.Line)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth((spent.toFloat() / d.budget).coerceIn(0f, 1f)).background(C.Accent))
                }
            }
        }

        // Панель выбранного предмета
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(Color(0xFFFBF4E3))
                .border(1.5.dp, C.Line, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                T(sel?.label ?: "Выбери предмет", 30.sp, C.Ink, 700, caveat = true, modifier = Modifier.weight(1f))
                if (tintableCount > 0) T("покрашено $coloredCount из $tintableCount", 12.sp, C.Ink2, 700)
            }
            val s = sel
            if (s == null) {
                T("Нажми на предмет в комнате или выбери ниже. Щипок — приблизить.", 14.sp, C.Ink2, 600)
            } else {
                val it = s.item
                val needBuy = it != null && scene.needsBuying(it, st)
                if (needBuy && it != null) {
                    val price = it.price ?: 0
                    val ok = spent + price <= (d.budget ?: Int.MAX_VALUE)
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(C.Card).border(1.5.dp, C.Line, RoundedCornerShape(14.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(Modifier.weight(1f)) {
                            T("Купить: " + it.name, 15.sp, C.Ink, 800)
                            T(if (ok) "останется " + money((d.budget ?: 0) - spent - price) else "не хватает денег", 12.sp, if (ok) C.Ink2 else C.Red, 700)
                        }
                        Box(
                            Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).background(if (ok) C.Accent else C.Line)
                                .tap { if (ok) st.bought.add(it.name) else warn = true }.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) { T(money(price), 15.sp, if (ok) Color.White else C.Ink2, 800) }
                    }
                    if (warn) T("Всё сразу не купить. Выбирай главное.", 13.sp, C.Red, 700)
                } else {
                    if (s.tintLayers.isNotEmpty()) {
                        val cur = st.tints[s.tintLayers.first()]
                        PencilRow(palette, cur, Modifier.fillMaxWidth()) { c -> s.tintLayers.forEach { l -> st.tints[l] = c } }
                        if (cur != null) Row { SmallChip("Вернуть цвет") { s.tintLayers.forEach { l -> st.tints.remove(l) } } }
                    } else {
                        T("Этот предмет уже в комнате.", 14.sp, C.Ink2, 600)
                    }
                    if (it != null && it.price != null && d.budget != null) {
                        Row { SmallChip("Убрать и вернуть ${money(it.price)}") { st.bought.remove(it.name); sel = null } }
                    }
                }
            }
            if (d.items.isNotEmpty()) {
                T("Предметы", 12.sp, C.Ink2, 800)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    d.items.forEach { item ->
                        val have = !scene.needsBuying(item, st)
                        val label = item.name + if (item.price != null && d.budget != null) (if (have) " ✓" else " · " + money(item.price)) else ""
                        SmallChip(label, on = sel?.key == item.name) { sel = selFor(item); warn = false }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LiveStep(scene: Scene, a: SceneAssets, speakers: Map<String, Speaker>, st: DesignState, onNext: () -> Unit) {
    var idx by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    val picked = remember(scene.id) { mutableStateMapOf<Int, String>() }
    var log by remember { mutableStateOf(false) }
    val lines = scene.dialog
    val line = lines[idx]
    val waiting = line.choices.isNotEmpty() && idx !in picked
    val text = scene.fill(picked[idx] ?: line.text, st)

    val zoom = if (line.camera.startsWith("zoom")) 1.5f else 1f
    val avgX = if (scene.stage.isEmpty()) 0.5f else scene.stage.values.map { it.first }.average().toFloat()
    val area = scene.cameraAreas[line.camera] ?: (avgX to 0.55f)
    val camZoom by animateFloatAsState(zoom, tween(900, easing = FastOutSlowInEasing), label = "z")
    val camX by animateFloatAsState(area.first, tween(900, easing = FastOutSlowInEasing), label = "x")
    val camY by animateFloatAsState(area.second, tween(900, easing = FastOutSlowInEasing), label = "y")
    val breathe by rememberInfiniteTransition(label = "br").animateFloat(
        1f, 1.012f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b",
    )

    val ids = remember(scene.id) { scene.stage.keys.toList() }
    val placed = remember(a, scene.id) {
        ids.mapNotNull { id ->
            val p = scene.stage[id] ?: return@mapNotNull null
            val img = a.charImg[id] ?: return@mapNotNull null
            val rel = a.chars[id]?.relHeight ?: 1f
            id to Placed(img, p.first * a.art.width, p.second * a.art.height, 0.37f * a.art.height * rel)
        }.toMap()
    }
    val hidden = scene.hiddenLayers(st) + "varya"
    val advance: () -> Unit = { if (!waiting) { if (idx < lines.lastIndex) idx++ else onNext() } }
    var bubbleW by remember { mutableIntStateOf(0) }
    var bubbleH by remember { mutableIntStateOf(0) }
    var viewW by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(340.dp).onSizeChanged { viewW = it.width }.tap(advance)) {
            SceneCanvas(
                a, Modifier.fillMaxSize().graphicsLayer { scaleX = breathe; scaleY = breathe },
                hidden = hidden, tints = st.tints, zoom = camZoom, fx = camX, fy = camY, placed = placed.values.toList(),
            ) { cam ->
                val spr = placed[line.who]
                if (spr != null && text.isNotEmpty()) {
                    val headX = cam.ox + spr.footX * cam.s
                    val headTop = cam.oy + (spr.footY - spr.heightPx) * cam.s
                    Box(
                        Modifier
                            .offset {
                                val maxX = (viewW - bubbleW - 8.dp.roundToPx()).coerceAtLeast(8.dp.roundToPx())
                                IntOffset(
                                    (headX - bubbleW / 2f).roundToInt().coerceIn(8.dp.roundToPx(), maxX),
                                    (headTop - bubbleH - 6.dp.toPx()).roundToInt().coerceAtLeast(8.dp.roundToPx()),
                                )
                            }
                            .widthIn(max = 260.dp).onSizeChanged { bubbleW = it.width; bubbleH = it.height }
                            .clip(RoundedCornerShape(18.dp)).background(Color(0xF5FFFAF0)).border(1.5.dp, C.Line, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) { T(text, 19.sp, C.Ink, 500, caveat = true, lineHeight = 22.sp) }
                }
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val who = speakers[line.who]?.name ?: line.who
            T(if (waiting) "$who скажет… выбери:" else who, 14.sp, C.Accent, 800)
            if (waiting) {
                line.choices.forEach { c ->
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp)).background(C.Card)
                            .border(1.5.dp, C.Accent, RoundedCornerShape(14.dp)).tap { picked[idx] = c }.padding(14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) { T("«$c»", 15.sp, C.Ink, 700, lineHeight = 21.sp) }
                }
            } else {
                T("«$text»", 16.sp, C.Ink, 400, lineHeight = 23.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundButton(size = 52, onClick = { log = true }) { T("☰", 18.sp, C.Ink, 800) }
            Box(Modifier.weight(1f)) { PrimaryButton(if (idx < lines.lastIndex) "Дальше" else "К заданию", enabled = !waiting, onClick = advance) }
        }
    }
    if (log) Dialog(onDismissRequest = { log = false }) {
        Column(Modifier.clip(RoundedCornerShape(20.dp)).background(C.Paper).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T("Что говорили", 28.sp, C.Ink, 700, caveat = true)
            for (i in 0..idx) {
                val l = lines[i]
                val t = scene.fill(picked[i] ?: l.text, st)
                if (t.isNotEmpty()) Column {
                    T(speakers[l.who]?.name ?: l.who, 13.sp, C.Accent, 800)
                    T("«$t»", 15.sp, C.Ink, 400, lineHeight = 21.sp)
                }
            }
            SecondaryButton("Закрыть", Modifier.fillMaxWidth()) { log = false }
        }
    }
}

@Composable
private fun CardStep(scene: Scene, hasNext: Boolean, onSaveCard: () -> Unit, onFinish: (Boolean) -> Unit) {
    LaunchedEffect(scene.id) { onSaveCard() }
    Column(Modifier.fillMaxSize()) {
        T("Новая карточка в Варином блокноте", 15.sp, C.Ink2, 800, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 6.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)) { CardSheet(scene.card) }
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("В блокнот", Modifier.weight(1f)) { onFinish(false) }
            Box(Modifier.weight(1.2f)) { PrimaryButton(if (hasNext) "Следующая сцена" else "К списку сцен") { onFinish(hasNext) } }
        }
    }
}
