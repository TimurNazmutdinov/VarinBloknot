package com.example.varinbloknot.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.logic.QuizGame
import com.example.varinbloknot.logic.QuizOutcome
import com.example.varinbloknot.logic.SortGame
import com.example.varinbloknot.logic.SortOutcome
import com.example.varinbloknot.logic.buildPlan
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TaskStep(scene: Scene, st: DesignState, onDone: () -> Unit) {
    val task = scene.task
    when (task.type) {
        "sort" -> SortDeck(task.title, task.bins, task.sortItems, onDone)
        "plan" -> PlanTask(scene, st, onDone)
        else -> QuizBody(task.title, task.question, task.options, task.correct, task.explain, task.hint, onDone)
    }
}

@Composable
private fun TaskHeader(title: String, hint: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        T(title, 40.sp, C.Ink, 700, caveat = true, lineHeight = 42.sp)
        if (hint.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            T(hint, 15.sp, C.Ink2, 600, lineHeight = 22.sp)
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun Note(text: String, bg: Color, border: Color, fg: Color) {
    Box(
        Modifier.padding(horizontal = 18.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg)
            .border(1.5.dp, border, RoundedCornerShape(14.dp)).padding(12.dp),
    ) { T(text, 14.sp, fg, 700, lineHeight = 19.sp) }
}

// ---------- «Нужно или хочется?»: колода карточек ----------

@Composable
private fun SortDeck(title: String, bins: List<String>, items: List<com.example.varinbloknot.data.SortItem>, onDone: () -> Unit) {
    val game = remember { SortGame(items) }
    var tick by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<SortOutcome?>(null) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val dx = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    @Suppress("UNUSED_EXPRESSION") tick

    val left = bins.getOrElse(0) { "Нужно" }
    val right = bins.getOrElse(1) { "Хочется" }

    fun answer(bin: String) {
        val o = game.answer(bin) ?: return
        feedback = o
        tick++
        haptic.performHapticFeedback(if (o is SortOutcome.Wrong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
        scope.launch {
            if (o is SortOutcome.Wrong) {
                for (v in listOf(-18f, 18f, -12f, 12f, 0f)) shake.animateTo(v, tween(60))
            }
            dx.snapTo(0f)
        }
    }

    val cur = game.current
    StepLayout(bottom = {
        when {
            game.finished -> PrimaryButton("Готово", onClick = onDone)
            else -> PrimaryButton(if (game.canAdvance) "Дальше" else "Выбери ответ", enabled = game.canAdvance) {
                game.advance(); feedback = null; tick++
            }
        }
    }) {
        TaskHeader(title, if (game.finished) "" else "Карточка ${game.index + 1} из ${items.size} · смахни или нажми кнопку")
        if (cur != null) {
            val outline = when (feedback) {
                is SortOutcome.Correct -> Color(0xFF7DB86C)
                is SortOutcome.Revealed -> Color(0xFFE0A13A)
                is SortOutcome.Wrong -> Color(0xFFD9695F)
                else -> C.Line
            }
            Box(Modifier.fillMaxWidth().height(250.dp).padding(horizontal = 40.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().rotate(4f).graphicsLayer { translationX = 10f }.clip(RoundedCornerShape(24.dp)).background(Color(0xFFF1E7D0)))
                Column(
                    Modifier.fillMaxSize()
                        .offset { IntOffset((dx.value + shake.value).roundToInt(), 0) }
                        .rotate(dx.value / 40f)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)).background(C.Card).border(3.dp, outline, RoundedCornerShape(24.dp))
                        .pointerInput(game.index, feedback) {
                            if (game.canAdvance) return@pointerInput
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    val v = dx.value
                                    scope.launch {
                                        if (v > 120f) { dx.snapTo(0f); answer(right) }
                                        else if (v < -120f) { dx.snapTo(0f); answer(left) }
                                        else dx.animateTo(0f, tween(150))
                                    }
                                },
                                onDragCancel = { scope.launch { dx.animateTo(0f, tween(150)) } },
                            ) { c, d -> c.consume(); scope.launch { dx.snapTo((dx.value + d).coerceIn(-300f, 300f)) } }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    if (cur.emoji.isNotEmpty()) T(cur.emoji, 76.sp)
                    T(cur.name, 26.sp, C.Ink, 800, align = TextAlign.Center)
                    if (cur.caption.isNotEmpty()) T(cur.caption, 13.sp, C.Ink2, 700, align = TextAlign.Center)
                }
            }
            Spacer(Modifier.height(12.dp))
            when (val f = feedback) {
                is SortOutcome.Correct -> Note((if (f.firstTry) "Верно! " else "Теперь верно. ") + f.why, Color(0xFFE3EDDC), Color(0xFFB9D6AD), C.Green)
                is SortOutcome.Wrong -> Note("Подумай ещё. " + f.hint, Color(0xFFFDECEA), Color(0xFFF0B8B0), Color(0xFF8A2F25))
                is SortOutcome.Revealed -> Note("Правильный ответ: ${f.correctBin}. " + f.why, Color(0xFFFFF3D6), Color(0xFFE9CE8A), Color(0xFF7A5200))
                null -> Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(12.dp))
            val locked = game.canAdvance
            Row(Modifier.padding(horizontal = 18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnswerButton("← $left", Color(0xFFE3EDDC), C.Green, Color(0xFFB9D6AD), !locked, Modifier.weight(1f)) { answer(left) }
                AnswerButton("$right →", Color(0xFFF6E0E8), C.Accent, Color(0xFFE6B5C6), !locked, Modifier.weight(1f)) { answer(right) }
            }
        } else {
            // итог: две стопки
            Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                T("Верно с первого раза: ${game.firstTryCount} из ${items.size}", 16.sp, C.Ink, 800)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(left to Color(0xFFE3EDDC), right to Color(0xFFF6E0E8)).forEach { (bin, bg) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(bg).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            T(bin, 24.sp, C.Ink, 700, caveat = true)
                            game.inBin(bin).forEach { T(it.emoji + " " + it.name, 14.sp, C.Ink, 700) }
                        }
                    }
                }
                T("Нужное — то, без чего не обойтись. Хочется — приятное, но можно подождать.", 14.sp, C.Ink2, 600, lineHeight = 20.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun AnswerButton(text: String, bg: Color, fg: Color, border: Color, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(64.dp).clip(RoundedCornerShape(18.dp)).background(if (enabled) bg else Color(0xFFEFE8D8))
            .border(2.dp, if (enabled) border else C.Line, RoundedCornerShape(18.dp)).tap { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) { T(text, 18.sp, if (enabled) fg else Color(0xFFB0A089), 800) }
}

// ---------- «Сколько накопить?» — из чисел игрока ----------

@Composable
private fun PlanTask(scene: Scene, st: DesignState, onDone: () -> Unit) {
    val d = scene.design
    val plan = remember { d.budget?.let { buildPlan(scene.task, it, d.items, st.bought.toSet(), scene.id) } }
    if (plan == null) {
        StepLayout(bottom = { PrimaryButton("Дальше", onClick = onDone) }) {
            TaskHeader(scene.task.title, "")
            Note("Ты купила всё, что хотела. Это тоже бюджет: деньги распределены по плану.", Color(0xFFE3EDDC), Color(0xFFB9D6AD), C.Green)
        }
    } else {
        QuizBody(scene.task.title, plan.text, plan.options, plan.correct, plan.explain, plan.hint, onDone, note = "Числа взяты из твоей комнаты")
    }
}

// ---------- Викторина: две попытки ----------

@Composable
private fun QuizBody(
    title: String, question: String, options: List<String>, correct: Int, explain: String, hint: String,
    onDone: () -> Unit, note: String = "",
) {
    val game = remember { QuizGame(options.size, correct) }
    var tick by remember { mutableIntStateOf(0) }
    var last by remember { mutableStateOf<QuizOutcome?>(null) }
    val haptic = LocalHapticFeedback.current
    @Suppress("UNUSED_EXPRESSION") tick

    StepLayout(bottom = { PrimaryButton("Дальше", enabled = game.done, onClick = onDone) }) {
        TaskHeader(title, question.ifEmpty { "Выбери один ответ." })
        if (note.isNotEmpty()) T(note, 12.sp, C.Ink2, 700, modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
        Column(Modifier.padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEachIndexed { i, text ->
                val wrongPicked = game.isDisabled(i)
                val showRight = game.done && i == correct
                val bg = when { showRight -> Color(0xFFE3EDDC); wrongPicked -> Color(0xFFF6DDD8); else -> C.Card }
                val br = when { showRight -> C.Green; wrongPicked -> C.Red; else -> C.Line }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 58.dp).clip(RoundedCornerShape(16.dp)).background(bg)
                        .border(2.dp, br, RoundedCornerShape(16.dp))
                        .tap {
                            val o = game.choose(i)
                            if (o != null) { last = o; tick++; haptic.performHapticFeedback(if (o is QuizOutcome.Wrong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove) }
                        }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    T(text, 17.sp, C.Ink, 700, modifier = Modifier.weight(1f), lineHeight = 22.sp)
                    if (showRight) T("✓", 20.sp, C.Green, 800)
                    if (wrongPicked) T("✕", 20.sp, C.Red, 800)
                }
            }
            when (last) {
                is QuizOutcome.Wrong -> Note("Почти! " + hint.ifEmpty { "Подумай ещё и выбери другой ответ." }, Color(0xFFFDECEA), Color(0xFFF0B8B0), Color(0xFF8A2F25))
                QuizOutcome.Correct -> Note(explain.ifEmpty { "Верно!" }, Color(0xFFE3EDDC), Color(0xFFB9D6AD), C.Green)
                QuizOutcome.Revealed -> Note("Правильный ответ подсвечен. " + explain.removePrefix("Верно! "), Color(0xFFFFF3D6), Color(0xFFE9CE8A), Color(0xFF7A5200))
                null -> {}
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
