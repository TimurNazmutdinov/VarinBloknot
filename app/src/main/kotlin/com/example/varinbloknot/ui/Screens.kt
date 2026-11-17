package com.example.varinbloknot.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.varinbloknot.BuildConfig
import com.example.varinbloknot.data.Scene
import com.example.varinbloknot.update.AppRelease

@Composable
fun HomeScreen(
    scenes: List<Scene>, done: Set<Int>, status: String,
    onOpen: (Scene) -> Unit, onNotebook: () -> Unit, onCheckUpdates: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(C.Paper).systemBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                T("Варин блокнот", 44.sp, C.Ink, 700, caveat = true)
                T("Выбери сцену", 15.sp, C.Ink2, 600)
            }
            RoundButton(onClick = onNotebook, icon = { NotebookIcon() })
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(scenes, key = { _, it -> it.id }) { idx, s ->
                val locked = idx > 0 && scenes[idx - 1].id !in done
                Row(
                    Modifier.fillMaxWidth().graphicsLayer { alpha = if (locked) 0.55f else 1f }.clip(RoundedCornerShape(16.dp)).background(C.Card)
                        .border(1.dp, C.Line, RoundedCornerShape(16.dp)).tap { if (!locked) onOpen(s) }.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ArtImage(s.color, Modifier.size(96.dp, 80.dp).clip(RoundedCornerShape(8.dp)))
                    Column(Modifier.weight(1f)) {
                        T("Сцена ${s.id}", 13.sp, C.Ink2, 800)
                        T(s.title, 26.sp, C.Ink, 700, caveat = true, lineHeight = 26.sp)
                        Spacer(Modifier.height(2.dp))
                        if (locked) T("🔒 Сначала пройди сцену ${scenes[idx - 1].id}", 13.sp, C.Ink2, 800) else if (s.id in done) T("✓ пройдена", 13.sp, C.Green, 800) else T(s.tags.joinToString(" · "), 13.sp, C.Accent, 700)
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (status.isNotEmpty()) T(status, 13.sp, C.Green, 700)
            Box(Modifier.tap(onCheckUpdates).padding(8.dp)) { T("Версия ${BuildConfig.VERSION_NAME} · Проверить обновления", 13.sp, C.Ink2, 700) }
        }
    }
}

@Composable
fun NotebookScreen(scenes: List<Scene>, cards: Set<Int>, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
    val mine = scenes.filter { it.id in cards }
    Column(Modifier.fillMaxSize().background(C.Paper).systemBarsPadding()) {
        TopBar("Варин блокнот", -1, onBack = onBack, onNotebook = {})
        if (mine.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                T("Пока пусто. Пройди сцену — и здесь появится первая карточка.", 22.sp, C.Ink2, 500, caveat = true, align = TextAlign.Center)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { T("Карточек: ${mine.size}", 15.sp, C.Ink2, 800) }
                items(mine, key = { it.id }) { CardSheet(it.card) }
            }
        }
    }
}

@Composable
fun UpdateDialog(
    release: AppRelease, busy: Boolean, error: String?, done: Long, total: Long,
    onUpdate: () -> Unit, onCancel: () -> Unit, onBrowser: () -> Unit, onLater: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!busy) onLater() }) {
        Column(
            Modifier.clip(RoundedCornerShape(20.dp)).background(C.Paper).border(1.dp, C.Line, RoundedCornerShape(20.dp)).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            T("Есть новая версия", 30.sp, C.Ink, 700, caveat = true)
            T("Версия ${release.version} (сейчас ${BuildConfig.VERSION_NAME}). Обновить?", 16.sp, C.Ink, 600)
            if (release.notes.isNotBlank() && !busy) T(release.notes.trim().take(400), 14.sp, C.Ink2, 400)
            if (busy) {
                val frac = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else 0f
                T(
                    if (total > 0) "Скачиваю: %.1f из %.1f МБ (%d%%)".format(done / 1048576f, total / 1048576f, (frac * 100).toInt()) else "Скачиваю…",
                    15.sp, C.Accent, 800,
                )
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(C.Line)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(frac).background(C.Accent))
                }
                T("GitHub отдаёт файл небыстро, это нормально. Подожди, пока полоса дойдёт до конца.", 12.sp, C.Ink2, 600)
            }
            if (error != null) T(error, 14.sp, C.Red, 700)
            if (busy) {
                SecondaryButton("Отмена", Modifier.fillMaxWidth(), onCancel)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Позже", Modifier.weight(1f), onLater)
                    Box(Modifier.weight(1f)) { PrimaryButton(if (error != null) "Повторить" else "Обновить", onClick = onUpdate) }
                }
                if (error != null) SecondaryButton("Скачать в браузере", Modifier.fillMaxWidth(), onBrowser)
            }
        }
    }
}
