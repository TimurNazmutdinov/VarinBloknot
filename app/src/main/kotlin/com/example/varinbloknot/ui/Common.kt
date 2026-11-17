package com.example.varinbloknot.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.varinbloknot.data.Card
import com.example.varinbloknot.data.ContentSet
import com.example.varinbloknot.data.Speaker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalContent = compositionLocalOf<ContentSet> { error("content not provided") }

@Composable
fun rememberArt(name: String): ImageBitmap? {
    val c = LocalContent.current
    return produceState<ImageBitmap?>(null, name, c) {
        value = withContext(Dispatchers.IO) { c.bitmap(name)?.asImageBitmap() }
    }.value
}

@Composable
fun ArtImage(name: String, modifier: Modifier = Modifier, scale: ContentScale = ContentScale.Crop) {
    val b = rememberArt(name)
    if (b != null) Image(bitmap = b, contentDescription = null, modifier = modifier, contentScale = scale)
    else Box(modifier.background(C.Line))
}

fun Modifier.tap(onClick: () -> Unit): Modifier =
    this.clickable(interactionSource = MutableInteractionSource(), indication = null, onClick = onClick)

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) C.Accent else C.Line)
            .tap { if (enabled) onClick() },
        contentAlignment = Alignment.Center,
    ) { T(text, 17.sp, if (enabled) Color.White else C.Ink2, 800) }
}

@Composable
fun SecondaryButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(52.dp).clip(RoundedCornerShape(16.dp)).background(C.Chip).tap(onClick),
        contentAlignment = Alignment.Center,
    ) { T(text, 16.sp, C.Ink, 800) }
}

@Composable
fun RoundButton(modifier: Modifier = Modifier, size: Int = 44, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        modifier.size(size.dp).clip(CircleShape).background(C.Chip).tap(onClick),
        contentAlignment = Alignment.Center,
    ) { icon() }
}

@Composable
fun BackIcon() = Canvas(Modifier.size(20.dp)) {
    val w = 2.5.dp.toPx()
    drawLine(C.Ink, Offset(size.width * 0.65f, size.height * 0.15f), Offset(size.width * 0.3f, size.height * 0.5f), w, StrokeCap.Round)
    drawLine(C.Ink, Offset(size.width * 0.3f, size.height * 0.5f), Offset(size.width * 0.65f, size.height * 0.85f), w, StrokeCap.Round)
}

@Composable
fun NotebookIcon(color: Color = C.Ink) = Canvas(Modifier.size(20.dp)) {
    val w = 2.dp.toPx()
    drawRoundRect(color, Offset(size.width * 0.18f, size.height * 0.08f), androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.84f),
        androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(w))
    drawLine(color, Offset(size.width * 0.18f, size.height * 0.72f), Offset(size.width * 0.82f, size.height * 0.72f), w)
}

val StepNames = listOf("Начало", "Оформление", "Сцена", "Дело", "Блокнот")

@Composable
fun TopBar(title: String, step: Int, onBack: () -> Unit, onNotebook: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(onClick = onBack, icon = { BackIcon() })
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            T(title, 14.sp, C.Ink2, 800)
            Spacer(Modifier.height(5.dp))
            if (step >= 0) Dots(step)
        }
        RoundButton(onClick = onNotebook, icon = { NotebookIcon() })
    }
}

@Composable
fun Dots(cur: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until 5) {
            Box(
                Modifier.height(8.dp).width(if (i == cur) 20.dp else 8.dp).clip(RoundedCornerShape(4.dp))
                    .background(if (i <= cur) C.Accent else C.Line)
            )
        }
    }
}

/** Рисунок, приклеенный скотчем, как фото в альбоме. */
@Composable
fun Polaroid(modifier: Modifier = Modifier, tilt: Float = -1.2f, caption: String? = null, content: @Composable () -> Unit) {
    Box(modifier.padding(horizontal = 18.dp, vertical = 10.dp).rotate(tilt)) {
        Column(
            Modifier.fillMaxWidth()
                .background(C.Photo, RoundedCornerShape(3.dp))
                .border(1.dp, C.Line, RoundedCornerShape(3.dp))
                .padding(8.dp),
        ) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(2.dp))) { content() }
            Spacer(Modifier.height(if (caption != null) 4.dp else 12.dp))
            if (caption != null) T(caption, 20.sp, C.Ink2, 500, caveat = true, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        }
        Box(Modifier.align(Alignment.TopStart).offset((-14).dp, (-6).dp).rotate(-8f).size(56.dp, 16.dp).background(C.Tape))
        Box(Modifier.align(Alignment.TopEnd).offset(14.dp, (-6).dp).rotate(8f).size(56.dp, 16.dp).background(C.Tape))
    }
}

@Composable
fun SpeakerAvatar(s: Speaker?, size: Int = 52) {
    val m = Modifier.size(size.dp).clip(CircleShape)
    if (s?.avatar != null) {
        ArtImage(s.avatar, m.border(2.dp, C.Accent, CircleShape), ContentScale.Crop)
    } else {
        Box(m.background(Color(0xFFEBD9AC)), contentAlignment = Alignment.Center) {
            T(s?.letter ?: "?", (size * 0.5f).sp, C.Ink2, 700, caveat = true)
        }
    }
}

@Composable
fun Chip(text: String, bg: Color = C.AccentSoft, fg: Color = C.Accent) {
    Box(Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 12.dp, vertical = 5.dp)) {
        T(text, 13.sp, fg, 800)
    }
}

/** Лист тетради в линейку с полем слева. */
@Composable
fun CardSheet(card: Card, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFFD3DEEE)
    Box(
        modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFDF6))
            .border(1.dp, C.Line, RoundedCornerShape(14.dp))
            .drawBehind {
                val step = 26.dp.toPx()
                var y = step
                while (y < size.height) { drawLine(lineColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx()); y += step }
                drawLine(Color(0xFFE9A0B8), Offset(34.dp.toPx(), 0f), Offset(34.dp.toPx(), size.height), 2.dp.toPx())
            },
    ) {
        Column(Modifier.padding(start = 50.dp, end = 16.dp, top = 16.dp, bottom = 18.dp)) {
            Chip(card.subject, C.GreenBg, C.Green)
            Spacer(Modifier.height(4.dp))
            T(card.title, 38.sp, C.Ink, 700, caveat = true)
            Spacer(Modifier.height(8.dp))
            ArtImage(card.art, Modifier.fillMaxWidth().aspectRatio(2.1f).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.height(10.dp))
            T(card.text, 15.sp, C.Ink, 400, lineHeight = 26.sp)
            Spacer(Modifier.height(8.dp))
            T("Как у Вари: " + card.example, 24.sp, C.Accent, 500, caveat = true, lineHeight = 28.sp)
        }
    }
}
