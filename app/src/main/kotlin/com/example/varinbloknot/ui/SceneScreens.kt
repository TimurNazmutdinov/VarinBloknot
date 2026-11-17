package com.example.varinbloknot.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.varinbloknot.data.DesignItem
import com.example.varinbloknot.data.PaintObj
import com.example.varinbloknot.data.Progress
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.data.SceneProgress
import com.example.varinbloknot.data.Speaker
import com.example.varinbloknot.logic.Shop
import com.example.varinbloknot.logic.buildSteps
import com.example.varinbloknot.logic.fillText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Экран шага: прокручиваемое содержимое + кнопка внизу. */
@Composable
fun StepLayout(bottom: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = content)
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) { bottom() }
    }
}

/** Что игрок сделал на шаге «Оформление»: купленные предметы и выбранные цвета (по id предмета). */
class DesignState {
    val bought = mutableStateListOf<String>()
    val tints = mutableStateMapOf<String, Color>()
}

fun Scene.hiddenLayers(st: DesignState): Set<String> = Shop.hiddenLayers(design.budget, design.items, st.bought)
fun Scene.fill(text: String, st: DesignState) = fillText(text, design.budget, design.items, st.bought)

@Composable
fun SceneScreen(
    scene: Scene, speakers: Map<String, Speaker>, hasNext: Boolean, store: Progress,
    onExit: () -> Unit, onNotebook: () -> Unit,
    onSaveCard: () -> Unit, onFinish: (next: Boolean) -> Unit,
) {
    var loaded by remember(scene.id) { mutableStateOf(false) }
    var initial by remember(scene.id) { mutableStateOf<SceneProgress?>(null) }
    LaunchedEffect(scene.id) { initial = store.loadScene(scene.id); loaded = true }
    if (!loaded) {
        Box(Modifier.fillMaxSize().background(C.Paper).systemBarsPadding()) { LoadingBox() }
        return
    }
    val assets = rememberSceneAssets(scene)
    val scope = rememberCoroutineScope()
    val state = remember(scene.id) {
        DesignState().also { s ->
            initial?.let { p ->
                s.bought.addAll(p.bought.filter { n -> scene.design.items.any { it.name == n } })
                p.tints.forEach { (k, v) -> if (scene.design.paint.any { it.id == k }) s.tints[k] = Color(v) }
            }
        }
    }
    var step by remember(scene.id) { mutableIntStateOf((initial?.step ?: 0).coerceIn(0, 4)) }
    var sub by remember(scene.id) {
        mutableStateOf(initial?.sub ?: if (scene.design.brief != null) "brief" else if (scene.design.items.isNotEmpty()) "shop" else "paint")
    }
    val picks = remember(scene.id) { mutableStateMapOf<Int, String>().also { m -> initial?.picks?.let { m.putAll(it) } } }

    // всё, что игрок сделал, сохраняется само: вышел — вернётся туда же
    LaunchedEffect(step, sub, state.bought.toList(), state.tints.toMap(), picks.toMap()) {
        store.saveScene(scene.id, SceneProgress(step, sub, state.bought.toList(), state.tints.mapValues { it.value.toArgb() }, picks.toMap()))
    }
    val finish: (Boolean) -> Unit = { next -> scope.launch { store.clearScene(scene.id); onFinish(next) } }

    androidx.activity.compose.BackHandler { if (step == 0) onExit() else step-- }

    Column(Modifier.fillMaxSize().background(C.Paper).systemBarsPadding()) {
        TopBar("Сцена ${scene.id} · ${StepNames[step]}", step, onBack = { if (step == 0) onExit() else step-- }, onNotebook = onNotebook)
        Box(Modifier.weight(1f)) {
            when (step) {
                0 -> IntroStep(scene) { step = 1 }
                1 -> if (sub == "brief") BriefStep(scene, speakers) { sub = if (scene.design.items.isNotEmpty()) "shop" else "paint" }
                    else if (assets != null) DesignStep(scene, assets, state, sub, { sub = it }) { step = 2 } else LoadingBox()
                2 -> if (assets != null) LiveStep(scene, assets, speakers, state, picks) { step = 3 } else LoadingBox()
                3 -> TaskStep(scene, state) { step = 4 }
                else -> CardStep(scene, hasNext, onSaveCard, finish)
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

/** Поручение героя перед оформлением (мама ставит задачу один раз, до покупок). */
@Composable
private fun BriefStep(scene: Scene, speakers: Map<String, Speaker>, onStart: () -> Unit) {
    val b = scene.design.brief ?: return
    StepLayout(bottom = { PrimaryButton("Понятно, пойду выбирать →", onClick = onStart) }) {
        Polaroid(Modifier.padding(top = 6.dp)) { ArtImage(scene.color, Modifier.fillMaxWidth().aspectRatio(1.6f)) }
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 10.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp))
                .background(Color(0xF5FFFAF0)).border(1.5.dp, C.Line, RoundedCornerShape(18.dp)).padding(14.dp),
        ) {
            T(speakers[b.who]?.name ?: b.who, 13.sp, C.Accent, 800)
            Spacer(Modifier.height(4.dp))
            T("«${scene.fill(b.text, DesignState())}»", 22.sp, C.Ink, 500, caveat = true, lineHeight = 26.sp)
        }
    }
}

// ---------- Оформление: покупки → раскраска ----------

@Composable
private fun DesignStep(scene: Scene, a: SceneAssets, st: DesignState, sub: String, onSub: (String) -> Unit, onDone: () -> Unit) {
    val d = scene.design
    val palette = remember(scene.id) { d.palette.map { parseHex(it) } }
    val hasShop = d.items.isNotEmpty()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "p",
    )

    var armed by remember { mutableIntStateOf(1.coerceAtMost(palette.lastIndex)) }   // -1 = ластик
    var selPaint by remember { mutableStateOf<String?>(null) }
    var selItem by remember { mutableStateOf<String?>(null) }
    var msg by remember { mutableStateOf<String?>(null) }
    var fillId by remember { mutableStateOf("") }
    val fillT = remember { Animatable(1f) }
    var popLayers by remember { mutableStateOf<Set<String>>(emptySet()) }
    val popT = remember { Animatable(1f) }

    LaunchedEffect(msg) { if (msg != null) { delay(2600); msg = null } }

    val hidden = scene.hiddenLayers(st) + "varya"
    val ghosts = remember(hidden) { hidden.filter { !it.endsWith("glow") && it != "varya" }.toSet() }
    val available = d.paint.filter { it.layer !in hidden }
    val painted = available.count { it.id in st.tints }
    val spent = Shop.spent(d.items, st.bought)

    fun paintObj(o: PaintObj) {
        selPaint = o.id; selItem = null
        if (armed < 0) st.tints.remove(o.id) else st.tints[o.id] = palette[armed]
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (armed >= 0) { fillId = o.id; scope.launch { fillT.snapTo(0f); fillT.animateTo(1f, tween(420)) } }
    }
    fun buy(it: DesignItem) {
        if (!Shop.canBuy(d.budget, d.items, st.bought, it)) { msg = "Не хватает денег. Выбирай главное."; return }
        st.bought.add(it.name)
        popLayers = it.layers.toSet()
        scope.launch { popT.snapTo(0f); popT.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    fun sell(it: DesignItem) {
        st.bought.remove(it.name)
        d.paint.filter { p -> p.layer in it.layers }.forEach { p -> st.tints.remove(p.id) }
        if (selPaint != null && d.paint.firstOrNull { p -> p.id == selPaint }?.layer in it.layers) selPaint = null
    }

    val selectedLayers: Set<String> = buildSet {
        selPaint?.let { id -> d.paint.firstOrNull { it.id == id }?.let { add(it.layer) } }
        selItem?.let { n -> d.items.firstOrNull { it.name == n }?.layers?.filter { !it.endsWith("glow") }?.let { addAll(it) } }
    }

    StepLayout(bottom = {
        if (sub == "shop" && hasShop) PrimaryButton("Дальше: раскрашивать →") { onSub("paint") }
        else PrimaryButton("Оживить сцену ✨", onClick = onDone)
    }) {
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            SceneCanvas(
                a, Modifier.fillMaxSize(), paint = d.paint, hidden = hidden, ghosts = ghosts, selected = selectedLayers, tints = st.tints,
                fillAnim = fillId.takeIf { it.isNotEmpty() }?.let { it to fillT.value },
                pop = popLayers.takeIf { it.isNotEmpty() }?.let { it to popT.value },
                interactive = true,
                onTap = { fx, fy ->
                    val layer = hitLayer(a, hidden, ghosts, fx, fy)
                    val item = layer?.let { l -> d.items.firstOrNull { l in it.layers } }
                    val obj = layer?.let { paintAt(a, d.paint, it, fx, fy) }
                    when {
                        item != null && Shop.needsBuying(d.budget, item, st.bought) -> {
                            selItem = item.name; selPaint = null; onSub("shop")
                        }
                        sub == "shop" && item != null -> { selItem = item.name; selPaint = null }
                        sub == "shop" -> { selPaint = null; selItem = null; msg = "Здесь ничего не продаётся. Светлые силуэты — то, что можно купить." }
                        obj != null && obj.layer !in hidden -> paintObj(obj)
                        else -> { selPaint = null; selItem = null; msg = "Это не красится. Нажми на предмет со значком-каплей." }
                    }
                },
                drawOver = { cam ->
                    if (sub == "paint") {
                        val r0 = 11.dp.toPx() * (1f + 0.16f * pulse)
                        for (o in available) {
                            if (o.id in st.tints) continue
                            val an = a.anchors[o.id] ?: continue
                            val c = Offset(cam.ox + an.x * cam.s, cam.oy + an.y * cam.s)
                            drawCircle(Color.White, r0 + 3.dp.toPx(), c)
                            drawCircle(C.Accent, r0, c)
                            val drop = Path().apply {
                                moveTo(c.x, c.y - r0 * 0.62f)
                                cubicTo(c.x + r0 * 0.6f, c.y - r0 * 0.02f, c.x + r0 * 0.46f, c.y + r0 * 0.56f, c.x, c.y + r0 * 0.56f)
                                cubicTo(c.x - r0 * 0.46f, c.y + r0 * 0.56f, c.x - r0 * 0.6f, c.y - r0 * 0.02f, c.x, c.y - r0 * 0.62f)
                                close()
                            }
                            drawPath(drop, Color.White)
                        }
                    }
                },
            )
            msg?.let {
                Box(
                    Modifier.align(Alignment.BottomCenter).padding(8.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xEE3D2F27)).padding(horizontal = 12.dp, vertical = 8.dp),
                ) { T(it, 13.sp, Color.White, 700) }
            }
        }

        // что делать сейчас
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFF8E6))
                .border(1.5.dp, Color(0xFFECD9A8), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            val title = when {
                !hasShop -> "Раскраска"
                sub == "shop" -> "Шаг 1 из 2 · покупки"
                else -> "Шаг 2 из 2 · раскраска"
            }
            T(title.uppercase(), 11.sp, C.Accent, 800)
            T((if (sub == "shop" && hasShop) d.shopHint.replace("{budget}", money(d.budget ?: 0)) else d.paintHint), 14.sp, C.Ink, 700, lineHeight = 18.sp)
        }

        if (hasShop) {
            Row(Modifier.padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallChip("🛒 Покупки", on = sub == "shop") { onSub("shop") }
                SmallChip("🎨 Раскраска", on = sub == "paint") { onSub("paint") }
            }
            if (d.budget != null) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        T("Потрачено ${money(spent)}", 13.sp, C.Ink, 800)
                        T("осталось ${money(d.budget - spent)}", 13.sp, C.Accent, 800)
                    }
                    Box(Modifier.padding(top = 4.dp).fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(C.Line)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth((spent.toFloat() / d.budget).coerceIn(0f, 1f)).background(C.Accent))
                    }
                }
            }
        }

        if (sub == "shop" && hasShop) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                d.items.forEach { it ->
                    val own = it.name in st.bought
                    val sel = selItem == it.name
                    val can = Shop.canBuy(d.budget, d.items, st.bought, it)
                    val thumb = a.img[it.layers.firstOrNull { l -> !l.endsWith("glow") } ?: it.layers.first()]
                    Column(
                        Modifier.width(120.dp).clip(RoundedCornerShape(14.dp))
                            .background(if (own) Color(0xFFEEF5EA) else C.Card)
                            .border(if (sel) 2.5.dp else 1.5.dp, if (sel) C.Accent else if (own) Color(0xFFB9D6AD) else C.Line, RoundedCornerShape(14.dp))
                            .tap { selItem = it.name; selPaint = null }.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(Modifier.height(62.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (thumb != null) Image(thumb, null, Modifier.fillMaxSize().padding(2.dp), contentScale = ContentScale.Fit)
                        }
                        T(it.name, 13.sp, C.Ink, 800)
                        Spacer(Modifier.height(4.dp))
                        val (label, bg, fg) = when {
                            own && sel -> Triple("Вернуть +" + money(it.price ?: 0), C.Chip, C.Ink)
                            own -> Triple("✓ куплено", Color.Transparent, C.Green)
                            sel && can -> Triple("Купить · " + money(it.price ?: 0), C.Accent, Color.White)
                            sel -> Triple("Не хватает", C.Line, C.Ink2)
                            else -> Triple(money(it.price ?: 0), C.Chip, if (can) C.Ink else Color(0xFFB0A089))
                        }
                        Box(
                            Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(10.dp)).background(bg)
                                .then(if (sel) Modifier.tap { if (own) sell(it) else buy(it) } else Modifier),
                            contentAlignment = Alignment.Center,
                        ) { T(label, 13.sp, fg, 800, align = androidx.compose.ui.text.style.TextAlign.Center) }
                    }
                }
            }
        } else {
            // раскраска: карандаш «в руке» + список предметов
            Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PencilRow(
                    palette, if (armed >= 0) palette[armed] else null, Modifier.fillMaxWidth(), eraserOn = armed < 0, onEraser = {
                        armed = -1
                        selPaint?.let { st.tints.remove(it) }
                    },
                ) { c ->
                    armed = palette.indexOf(c)
                    selPaint?.let { id -> d.paint.firstOrNull { it.id == id }?.let { paintObj(it) } }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    available.forEach { o ->
                        val thumb = a.img[o.overlay ?: o.layer]
                        val col = st.tints[o.id]
                        val sel = selPaint == o.id
                        Column(
                            Modifier.width(72.dp).clip(RoundedCornerShape(12.dp)).background(C.Card)
                                .border(if (sel) 2.5.dp else 1.5.dp, if (sel) C.Accent else C.Line, RoundedCornerShape(12.dp))
                                .tap { paintObj(o) }.padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(Modifier.height(34.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                if (thumb != null) Image(thumb, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                if (col != null) Box(Modifier.align(Alignment.TopEnd).size(12.dp).clip(androidx.compose.foundation.shape.CircleShape).background(col).border(1.5.dp, Color.White, androidx.compose.foundation.shape.CircleShape))
                            }
                            T(o.label, 11.sp, C.Ink, 800, maxLines = 1)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    T("Покрашено $painted из ${available.size}", 13.sp, C.Ink, 800)
                    T(available.joinToString(" ") { if (it.id in st.tints) "●" else "○" }, 13.sp, C.Green, 800)
                }
                if (painted == available.size && available.isNotEmpty()) T("Красиво! Можно оживлять ✨", 13.sp, C.Green, 800)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ---------- Живая сцена (в 0.3.1 будет переделана в «комикс» с условиями) ----------

@Composable
private fun LiveStep(
    scene: Scene, a: SceneAssets, speakers: Map<String, Speaker>, st: DesignState,
    picks: MutableMap<Int, String>, onNext: () -> Unit,
) {
    var idx by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    var log by remember { mutableStateOf(false) }
    val steps = buildSteps(scene.dialog, picks, scene.design.budget, scene.design.items, st.bought)
    if (steps.isEmpty()) { LaunchedEffect(scene.id) { onNext() }; return }
    val cur = idx.coerceIn(0, steps.lastIndex)
    val line = steps[cur]
    val waiting = line.choices.isNotEmpty()
    val text = scene.fill(line.text, st)

    val zoom = if (line.camera.startsWith("zoom")) 1.5f else 1f
    val avgX = if (scene.stage.isEmpty()) 0.5f else scene.stage.values.map { it.first }.average().toFloat()
    val area = scene.cameraAreas[line.camera] ?: (avgX to 0.55f)
    val camZoom by animateFloatAsState(zoom, tween(900, easing = FastOutSlowInEasing), label = "z")
    val camX by animateFloatAsState(area.first, tween(900, easing = FastOutSlowInEasing), label = "x")
    val camY by animateFloatAsState(area.second, tween(900, easing = FastOutSlowInEasing), label = "y")
    val breathe by rememberInfiniteTransition(label = "br").animateFloat(
        1f, 1.012f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b",
    )

    val placed = remember(a, scene.id) {
        scene.stage.keys.toList().mapNotNull { id ->
            val p = scene.stage[id] ?: return@mapNotNull null
            val img = a.charImg[id] ?: return@mapNotNull null
            val rel = a.chars[id]?.relHeight ?: 1f
            id to Placed(img, p.first * a.art.width, p.second * a.art.height, 0.37f * a.art.height * rel)
        }.toMap()
    }
    val hidden = scene.hiddenLayers(st) + "varya"
    val advance: () -> Unit = { if (!waiting) { if (cur < steps.lastIndex) idx = cur + 1 else onNext() } }
    var bubbleW by remember { mutableIntStateOf(0) }
    var bubbleH by remember { mutableIntStateOf(0) }
    var viewW by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(340.dp).onSizeChanged { viewW = it.width }.tap(advance)) {
            SceneCanvas(
                a, Modifier.fillMaxSize().graphicsLayer { scaleX = breathe; scaleY = breathe },
                paint = scene.design.paint, hidden = hidden, tints = st.tints, zoom = camZoom, fx = camX, fy = camY, placed = placed.values.toList(),
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
            T(if (waiting) (if (line.who == "varya") "Ты — Варя. Что ответишь?" else "$who скажет… выбери:") else who, 14.sp, C.Accent, 800)
            if (waiting) {
                line.choices.forEach { c ->
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp)).background(C.Card)
                            .border(1.5.dp, C.Accent, RoundedCornerShape(14.dp)).tap { picks[line.lineIdx] = c.text }.padding(14.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) { T("«${c.text}»", 15.sp, C.Ink, 700, lineHeight = 21.sp) }
                }
            } else {
                T("«$text»", 16.sp, C.Ink, 400, lineHeight = 23.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundButton(size = 52, onClick = { log = true }) { T("☰", 18.sp, C.Ink, 800) }
            Box(Modifier.weight(1f)) { PrimaryButton(if (cur < steps.lastIndex) "Дальше" else "К заданию", enabled = !waiting, onClick = advance) }
        }
    }
    if (log) Dialog(onDismissRequest = { log = false }) {
        Column(Modifier.clip(RoundedCornerShape(20.dp)).background(C.Paper).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            T("Что говорили", 28.sp, C.Ink, 700, caveat = true)
            for (i in 0..cur) {
                val l = steps[i]
                val t = scene.fill(l.text, st)
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
