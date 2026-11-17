package com.example.varinbloknot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.example.varinbloknot.data.*
import com.example.varinbloknot.ui.*
import com.example.varinbloknot.update.AppRelease
import com.example.varinbloknot.update.AppUpdater
import com.example.varinbloknot.update.ContentUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val progress = remember { Progress(ctx) }
    var content by remember { mutableStateOf<ContentSet?>(null) }
    var scenes by remember { mutableStateOf<List<Scene>>(emptyList()) }
    var speakers by remember { mutableStateOf<Map<String, Speaker>>(emptyMap()) }
    val done by progress.done.collectAsState(emptySet())
    val cards by progress.cards.collectAsState(emptySet())
    var screen by rememberSaveable { mutableStateOf("home") }
    var sceneId by rememberSaveable { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("") }
    var release by remember { mutableStateOf<AppRelease?>(null) }
    var busy by remember { mutableStateOf(false) }
    var updError by remember { mutableStateOf<String?>(null) }
    var dlDone by remember { mutableStateOf(0L) }
    var dlTotal by remember { mutableStateOf(0L) }
    var dlJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    suspend fun reload() {
        val c = withContext(Dispatchers.IO) { ContentStore.load(ctx) }
        val s = withContext(Dispatchers.IO) { c.scenes() }
        val sp = withContext(Dispatchers.IO) { c.speakers() }
        content = c; scenes = s; speakers = sp
    }

    /** Проверка обновлений контента и программы. force=true — игнорировать ограничение по времени. */
    suspend fun checkUpdates(force: Boolean): String {
        val now = System.currentTimeMillis()
        val v = content?.version ?: 0
        val (cChecked, cApplied) = withContext(Dispatchers.IO) { ContentUpdater.run(ctx, v, progress.contentCheckedAt(), now, force) }
        if (cChecked) progress.setContentChecked(now)
        if (cApplied) reload()
        val (aChecked, rel) = withContext(Dispatchers.IO) { AppUpdater.check(ctx, progress.appCheckedAt(), now, force) }
        if (aChecked) progress.setAppChecked(now)
        if (rel != null) { release = rel; updError = null }
        return when {
            rel != null -> "Есть новая версия ${rel.version}"
            cApplied -> "Контент обновлён"
            !cChecked && !aChecked && force -> "Нет сети — проверить не удалось"
            force -> "Всё свежее"
            else -> ""
        }
    }

    LaunchedEffect(Unit) {
        reload()
        checkUpdates(false)
    }

    val c = content
    if (c == null) return
    CompositionLocalProvider(LocalContent provides c) {
        val scene = scenes.firstOrNull { it.id == sceneId }
        when {
            screen == "notebook" -> NotebookScreen(scenes, cards) { screen = if (scene != null) "scene" else "home" }
            screen == "scene" && scene != null -> {
                val next = scenes.firstOrNull { it.id > scene.id }
                SceneScreen(
                    scene, speakers, hasNext = next != null, store = progress,
                    onExit = { screen = "home" },
                    onNotebook = { screen = "notebook" },
                    onSaveCard = { scope.launch { progress.addCard(scene.id); progress.markDone(scene.id) } },
                    onFinish = { goNext ->
                        if (goNext && next != null) sceneId = next.id else screen = if (goNext) "home" else "notebook"
                    },
                )
            }
            else -> HomeScreen(
                scenes, done, status,
                onOpen = { sceneId = it.id; screen = "scene" },
                onNotebook = { screen = "notebook" },
                onCheckUpdates = { scope.launch { status = "Проверяю…"; status = checkUpdates(true) } },
            )
        }
        release?.let { r ->
            UpdateDialog(r, busy, updError, dlDone, dlTotal, onUpdate = {
                dlJob = scope.launch {
                    busy = true; updError = null; dlDone = 0; dlTotal = r.size
                    try {
                        val f = withContext(Dispatchers.IO) {
                            AppUpdater.download(ctx, r, { !isActive }) { d, t -> dlDone = d; dlTotal = t }
                        }
                        AppUpdater.install(ctx, f)
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        updError = null
                    } catch (e: Exception) {
                        updError = "Не удалось скачать (медленная или оборванная связь). Можно повторить: скачивание продолжится с того же места."
                    }
                    busy = false
                }
            }, onCancel = { dlJob?.cancel(); busy = false }, onBrowser = { AppUpdater.openInBrowser(ctx, r) }, onLater = { release = null })
        }
    }
}
