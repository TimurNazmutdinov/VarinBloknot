package com.example.varinbloknot.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.varinbloknot.data.Task
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TaskStep(task: Task, bought: Set<String>, onDone: () -> Unit) {
    when (task.type) {
        "budget" -> BudgetTask(task, bought, onDone)
        "sort" -> SortTask(task, onDone)
        else -> QuizTask(task, onDone)
    }
}

@Composable
private fun TaskHeader(title: String, hint: String) {
    Column(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        T(title, 40.sp, C.Ink, 700, caveat = true, lineHeight = 42.sp)
        Spacer(Modifier.height(6.dp))
        T(hint, 15.sp, C.Ink2, 400, lineHeight = 22.sp)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun BudgetTask(task: Task, bought: Set<String>, onDone: () -> Unit) {
    val sel = remember { mutableStateListOf<Int>().apply { task.budgetItems.forEachIndexed { i, (n, _) -> if (n in bought) add(i) } } }
    var warn by remember { mutableStateOf(false) }
    val sum = sel.sumOf { task.budgetItems[it].second }
    val over = sum > task.limit
    StepLayout(bottom = {
        PrimaryButton("Готово") { if (sel.isEmpty() || over) warn = true else onDone() }
    }) {
        TaskHeader(task.title, if (bought.isEmpty()) "Выбери, что Варя купит для комнаты. Сумма не должна быть больше бюджета." else "Вот что ты выбрала для комнаты. Проверь: хватает ли денег? Можно поменять.")
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            task.budgetItems.forEachIndexed { i, (name, price) ->
                val on = i in sel
                Row(
                    Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(14.dp)).background(C.Card)
                        .border(if (on) 1.5.dp else 1.dp, if (on) C.Accent else C.Line, RoundedCornerShape(14.dp))
                        .tap { if (on) sel.remove(i) else sel.add(i); warn = false }.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(24.dp).clip(RoundedCornerShape(5.dp)).background(if (on) C.Accent else Color.Transparent)
                            .border(1.5.dp, if (on) C.Accent else C.Ink2, RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center,
                    ) { if (on) T("✓", 16.sp, Color.White, 800) }
                    Spacer(Modifier.width(14.dp))
                    T(name, 16.sp, C.Ink, 600, modifier = Modifier.weight(1f))
                    T(money(price), 16.sp, C.Ink, 800)
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (over) C.RedBg else C.GreenBg).padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val col = if (over) C.Red else C.Green
                T("Выбрано: ${money(sum)}", 15.sp, col, 800)
                T(if (over) "Больше на ${money(sum - task.limit)}" else "Осталось: ${money(task.limit - sum)}", 15.sp, col, 800)
            }
            if (warn) T(
                if (over) "Многовато! Убери что-нибудь: сумма больше ${money(task.limit)}." else "Выбери хотя бы одну вещь.",
                14.sp, C.Red, 700,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SortTask(task: Task, onDone: () -> Unit) {
    val bins = remember { mutableStateMapOf<Int, Rect>() }
    val placed = remember { mutableStateMapOf<Int, Int>() } // предмет -> корзина
    var msg by remember { mutableStateOf("") }
    LaunchedEffect(msg) { if (msg.isNotEmpty()) { delay(1800); msg = "" } }
    val all = placed.size == task.sortItems.size

    StepLayout(bottom = { PrimaryButton("Готово", enabled = all, onClick = onDone) }) {
        TaskHeader(task.title, "Перетащи каждую вещь в нужную колонку.")
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            task.bins.forEachIndexed { b, name ->
                Column(
                    Modifier.weight(1f).heightIn(min = 190.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (b == 0) C.GreenBg else C.AccentSoft)
                        .border(1.5.dp, C.Line, RoundedCornerShape(14.dp))
                        .onGloballyPositioned { bins[b] = it.boundsInRoot() }.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    T(name, 24.sp, if (b == 0) C.Green else C.Accent, 700, caveat = true)
                    task.sortItems.forEachIndexed { i, (n, _) ->
                        if (placed[i] == b) Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(C.Card).padding(8.dp)) { T(n, 14.sp, C.Ink, 600) }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            task.sortItems.forEachIndexed { i, (name, right) ->
                if (i in placed) return@forEachIndexed
                DragChip(name) { center ->
                    val b = bins.entries.firstOrNull { it.value.contains(center) }?.key
                    if (b != null) {
                        if (task.bins[b] == right) placed[i] = b else msg = "Не сюда. Подумай ещё!"
                    }
                }
            }
        }
        if (msg.isNotEmpty()) T(msg, 14.sp, C.Red, 700, modifier = Modifier.padding(20.dp))
        if (all) T("Всё разложено верно!", 18.sp, C.Green, 800, modifier = Modifier.padding(20.dp))
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun DragChip(text: String, onDrop: (Offset) -> Unit) {
    var off by remember { mutableStateOf(Offset.Zero) }
    var pos by remember { mutableStateOf(Offset.Zero) }
    var sz by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    Box(
        Modifier
            .zIndex(if (dragging) 10f else 0f)
            .offset { IntOffset(off.x.roundToInt(), off.y.roundToInt()) }
            .onGloballyPositioned { pos = it.positionInRoot(); sz = Offset(it.size.width.toFloat(), it.size.height.toFloat()) }
            .shadow(if (dragging) 8.dp else 1.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)).background(if (dragging) Color(0xFFFFF3B8) else C.Card)
            .border(1.dp, C.Line, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDrag = { c, d -> c.consume(); off += d },
                    onDragEnd = { onDrop(pos + Offset(sz.x / 2, sz.y / 2)); off = Offset.Zero; dragging = false },
                    onDragCancel = { off = Offset.Zero; dragging = false },
                )
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) { T(text, 15.sp, C.Ink, 700) }
}

@Composable
private fun QuizTask(task: Task, onDone: () -> Unit) {
    var chosen by remember { mutableStateOf<Int?>(null) }
    StepLayout(bottom = { PrimaryButton("Дальше", enabled = chosen != null, onClick = onDone) }) {
        TaskHeader(task.title, "Выбери один ответ.")
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            task.options.forEachIndexed { i, text ->
                val c = chosen
                val state = when {
                    c == null -> 0
                    i == task.correct -> 1
                    i == c -> 2
                    else -> 0
                }
                val bg = when (state) { 1 -> C.GreenBg; 2 -> C.RedBg; else -> C.Card }
                val br = when (state) { 1 -> C.Green; 2 -> C.Red; else -> C.Line }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg)
                        .border(1.5.dp, br, RoundedCornerShape(14.dp))
                        .tap { if (chosen == null) chosen = i }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    T(text, 16.sp, C.Ink, 600, modifier = Modifier.weight(1f), lineHeight = 22.sp)
                    if (state == 1) T("✓", 20.sp, C.Green, 800)
                    if (state == 2) T("✕", 20.sp, C.Red, 800)
                }
            }
            val c = chosen
            if (c != null) {
                T(if (c == task.correct) task.explain else "Почти! Правильный ответ подсвечен. " + task.explain.removePrefix("Верно! "), 16.sp, C.Ink, 600, lineHeight = 23.sp)
            }
        }
    }
}
