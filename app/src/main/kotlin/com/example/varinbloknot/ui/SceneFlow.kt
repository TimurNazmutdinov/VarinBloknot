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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.data.Speaker
import kotlinx.coroutines.delay

/** Экран шага: прокручиваемое содержимое + кнопка внизу. */
@Composable
fun StepLayout(bottom: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = content)
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp)) { bottom() }
    }
}

@Composable
fun SceneScreen(
    scene: Scene, speakers: Map<String, Speaker>, hasNext: Boolean,
    onExit: () -> Unit, onNotebook: () -> Unit,
    onSaveCard: () -> Unit, onFinish: (next: Boolean) -> Unit,
) {
    var step by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    val paint = remember(scene.id) { PaintState() }
    val placed = remember(scene.id) { mutableStateListOf<String>() }
    androidx.activity.compose.BackHandler { if (step == 0) onExit() else step-- }

    Column(Modifier.fillMaxSize().background(C.Paper).systemBarsPadding()) {
        TopBar("Сцена ${scene.id} · ${StepNames[step]}", step, onBack = { if (step == 0) onExit() else step-- }, onNotebook = onNotebook)
        Box(Modifier.weight(1f)) {
            when (step) {
                0 -> IntroStep(scene) { step = 1 }
                1 -> DesignStep(scene, paint, placed) { step = 2 }
                2 -> LiveStep(scene, speakers) { step = 3 }
                3 -> TaskStep(scene.task) { step = 4 }
                else -> CardStep(scene, hasNext, onSaveCard, onFinish)
            }
        }
    }
}

@Composable
private fun IntroStep(scene: Scene, onStart: () -> Unit) {
    StepLayout(bottom = { PrimaryButton("Начать", onClick = onStart) }) {
        Polaroid(Modifier.padding(top = 6.dp)) { ArtImage(scene.color, Modifier.fillMaxWidth().aspectRatio(780f / 640f)) }
        Column(Modifier.padding(horizontal = 22.dp, vertical = 6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { scene.tags.forEach { Chip(it) } }
            Spacer(Modifier.height(8.dp))
            T(scene.title, 46.sp, C.Ink, 700, caveat = true, lineHeight = 48.sp)
            Spacer(Modifier.height(6.dp))
            T(scene.story, 17.sp, C.Ink, 400, lineHeight = 26.sp)
        }
    }
}

private val StickerColors = listOf(Color(0xFFFFF3B8), Color(0xFFFBE0E8), Color(0xFFDCEFD5), Color(0xFFDDE9F8))

@Composable
private fun DesignStep(scene: Scene, paint: PaintState, placed: MutableList<String>, onDone: () -> Unit) {
    val d = scene.design
    val palette = remember(scene.id) { d.palette.map { parseHex(it) } }
    LaunchedEffect(scene.id) { if (paint.color !in palette && palette.isNotEmpty()) paint.color = palette[1 % palette.size] }
    val gray = rememberArt(scene.gray)
    val color = rememberArt(scene.color)
    var painting by rememberSaveable(scene.id) { mutableStateOf(true) }
    var full by remember { mutableStateOf(false) }
    var tab by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    var warn by remember { mutableStateOf(false) }
    val reveal = remember { Animatable(0f) }
    var revealing by remember { mutableStateOf(false) }

    LaunchedEffect(revealing) {
        if (revealing) { reveal.animateTo(1f, tween(1500, easing = LinearEasing)); delay(250); onDone() }
    }
    LaunchedEffect(warn) { if (warn) { delay(2200); warn = false } }

    val spent = d.items.filter { it.name in placed }.sumOf { it.price ?: 0 }
    val ratio = if (gray != null) gray.width.toFloat() / gray.height else 780f / 640f

    StepLayout(bottom = { PrimaryButton("Готово, оживить сцену", enabled = !revealing) { revealing = true } }) {
        Polaroid(Modifier.padding(top = 4.dp), caption = d.hint) {
            Box(Modifier.fillMaxWidth().aspectRatio(ratio)) {
                if (gray != null && color != null && revealing) RevealSurface(gray, color, paint, reveal.value, Modifier.fillMaxSize())
                else if (gray != null) PaintSurface(gray, paint, painting, Modifier.fillMaxSize())
                if (!revealing) {
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(8.dp).size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xE6FFFAF0)).tap { full = true },
                        contentAlignment = Alignment.Center,
                    ) { T("⛶", 20.sp, C.Ink, 700) }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 2.dp), contentAlignment = Alignment.Center) { ModeToggle(painting) { painting = it } }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            d.tabs.forEachIndexed { i, t ->
                Box(
                    Modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(if (i == tab) C.Accent else C.Chip).tap { tab = i }.padding(horizontal = 12.dp, vertical = 8.dp),
                ) { T(t, 14.sp, if (i == tab) Color.White else C.Ink, 800) }
            }
        }
        PencilRow(palette, paint, Modifier.padding(horizontal = 12.dp).fillMaxWidth().horizontalScroll(rememberScrollState()).height(100.dp)
            .clip(RoundedCornerShape(topEnd = 14.dp)))
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            d.items.forEachIndexed { i, it ->
                val on = it.name in placed
                Column(
                    Modifier.width(108.dp).rotate(listOf(-1.5f, 1f, -0.6f, 1.2f)[i % 4])
                        .clip(RoundedCornerShape(6.dp)).background(StickerColors[i % StickerColors.size])
                        .then(if (on) Modifier.border(2.dp, C.Accent, RoundedCornerShape(6.dp)) else Modifier)
                        .tap {
                            if (on) placed.remove(it.name)
                            else if (d.budget != null && spent + (it.price ?: 0) > d.budget) warn = true
                            else placed.add(it.name)
                        }.padding(10.dp),
                ) {
                    T(it.name, 14.sp, C.Ink, 800)
                    if (it.price != null) T(money(it.price), 13.sp, C.Ink2, 600)
                    if (on) T("✓ на месте", 12.sp, C.Green, 800)
                }
            }
        }
        if (d.budget != null) {
            Row(Modifier.padding(horizontal = 18.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                T("Бюджет ${money(d.budget)}", 15.sp, C.Ink, 800)
                T("осталось ${money(d.budget - spent)}", 15.sp, C.Accent, 800)
            }
            Box(Modifier.padding(horizontal = 18.dp, vertical = 6.dp).fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(C.Line)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth((spent.toFloat() / d.budget).coerceIn(0f, 1f)).background(C.Accent))
            }
            if (warn) T("Не хватает денег — сними что-нибудь с картинки", 13.sp, C.Red, 700, modifier = Modifier.padding(horizontal = 18.dp))
        }
        Spacer(Modifier.height(6.dp))
    }
    if (full && gray != null) FullscreenColoring(gray, palette, paint, painting, { painting = it }) { full = false }
}

@Composable
private fun LiveStep(scene: Scene, speakers: Map<String, Speaker>, onNext: () -> Unit) {
    var idx by rememberSaveable(scene.id) { mutableIntStateOf(0) }
    val lines = scene.dialog
    val line = lines[idx]
    val zoom = line.camera.startsWith("zoom")
    val camScale by animateFloatAsState(if (zoom) 1.3f else 1f, tween(900, easing = FastOutSlowInEasing), label = "cam")
    val origin = scene.cameraAreas[line.camera] ?: (0.5f to 0.5f)
    val breathe = rememberInfiniteTransition(label = "breathe").animateFloat(
        1f, 1.01f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b",
    )
    val advance: () -> Unit = { if (idx < lines.lastIndex) idx++ else onNext() }
    val spk = speakers[line.who]

    Column(Modifier.fillMaxSize().tap(advance)) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(0.dp))) {
            ArtImage(
                scene.color,
                Modifier.fillMaxWidth().aspectRatio(780f / 640f).graphicsLayer {
                    val s = camScale * breathe.value
                    scaleX = s; scaleY = s
                    transformOrigin = TransformOrigin(origin.first, origin.second)
                },
            )
            Box(
                Modifier.align(Alignment.TopStart).padding(12.dp).clip(RoundedCornerShape(18.dp))
                    .background(Color(0xF5FFFAF0)).border(1.5.dp, C.Line, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            ) { T("«" + line.text + "»", 24.sp, C.Ink, 500, caveat = true, lineHeight = 27.sp) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            for (i in 0..idx) {
                val l = lines[i]
                val s = speakers[l.who]
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SpeakerAvatar(s)
                    Column {
                        T(s?.name ?: l.who, 14.sp, C.Accent, 800)
                        T("«" + l.text + "»", 16.sp, C.Ink, 400, lineHeight = 23.sp)
                    }
                }
            }
        }
        T("Нажми на экран, чтобы продолжить", 13.sp, C.Ink2, 600, modifier = Modifier.align(Alignment.CenterHorizontally))
        Box(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) { PrimaryButton(if (idx < lines.lastIndex) "Дальше" else "К заданию", onClick = advance) }
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
