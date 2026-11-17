package com.example.varinbloknot.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream

data class Speaker(val id: String, val name: String, val avatar: String?, val letter: String?)
data class DesignItem(val name: String, val price: Int?)
data class Design(
    val hint: String, val tabs: List<String>, val palette: List<String>,
    val budget: Int?, val items: List<DesignItem>,
)
data class Line(val who: String, val text: String, val camera: String)
data class Task(
    val type: String, val title: String,
    val limit: Int = 0, val budgetItems: List<Pair<String, Int>> = emptyList(),
    val bins: List<String> = emptyList(), val sortItems: List<Pair<String, String>> = emptyList(),
    val options: List<String> = emptyList(), val correct: Int = 0, val explain: String = "",
)
data class Card(val subject: String, val title: String, val art: String, val text: String, val example: String)
data class Scene(
    val id: Int, val title: String, val tags: List<String>, val story: String,
    val gray: String, val color: String, val design: Design, val dialog: List<Line>,
    val task: Task, val card: Card, val cameraAreas: Map<String, Pair<Float, Float>>,
)

private fun JSONArray.strings() = List(length()) { getString(it) }

fun parseScene(text: String): Scene {
    val j = JSONObject(text)
    val d = j.getJSONObject("design")
    val design = Design(
        d.getString("hint"), d.getJSONArray("tabs").strings(), d.getJSONArray("palette").strings(),
        if (d.isNull("budget")) null else d.getInt("budget"),
        d.getJSONArray("items").let { a ->
            List(a.length()) {
                val o = a.getJSONObject(it)
                DesignItem(o.getString("name"), if (o.isNull("price")) null else o.getInt("price"))
            }
        },
    )
    val dl = j.getJSONArray("dialog").let { a ->
        List(a.length()) {
            val o = a.getJSONObject(it)
            Line(o.getString("who"), o.getString("text"), o.optString("camera", "wide"))
        }
    }
    val t = j.getJSONObject("task")
    val type = t.getString("type")
    val task = when (type) {
        "budget" -> Task(type, t.getString("title"), limit = t.getInt("limit"),
            budgetItems = t.getJSONArray("items").let { a ->
                List(a.length()) { a.getJSONArray(it).let { p -> p.getString(0) to p.getInt(1) } }
            })
        "sort" -> Task(type, t.getString("title"), bins = t.getJSONArray("bins").strings(),
            sortItems = t.getJSONArray("items").let { a ->
                List(a.length()) { a.getJSONArray(it).let { p -> p.getString(0) to p.getString(1) } }
            })
        else -> Task(type, t.getString("title"), options = t.getJSONArray("options").strings(),
            correct = t.getInt("correct"), explain = t.optString("explain"))
    }
    val c = j.getJSONObject("card")
    val card = Card(c.getString("subject"), c.getString("title"), c.getString("art"), c.getString("text"), c.getString("example"))
    val areas = HashMap<String, Pair<Float, Float>>()
    j.optJSONObject("cameraAreas")?.let { o ->
        o.keys().forEach { k -> o.getJSONObject(k).let { areas[k] = it.getDouble("x").toFloat() to it.getDouble("y").toFloat() } }
    }
    val art = j.getJSONObject("art")
    return Scene(j.getInt("id"), j.getString("title"), j.getJSONArray("tags").strings(), j.getString("story"),
        art.getString("gray"), art.getString("color"), design, dl, task, card, areas)
}

/** Набор контента: либо внутри APK (assets), либо скачанный во внутреннюю память. */
class ContentSet(private val ctx: Context, private val dir: File?) {
    val version: Int = readManifestVersion()

    private fun open(path: String): InputStream =
        if (dir != null) File(dir, path).inputStream() else ctx.assets.open(path)

    private fun readManifestVersion(): Int = try {
        JSONObject(open("manifest.json").bufferedReader().readText()).getInt("contentVersion")
    } catch (e: Exception) { 0 }

    fun sceneFiles(): List<String> =
        (if (dir != null) File(dir, "scenes").list()?.toList() else ctx.assets.list("scenes")?.toList())
            .orEmpty().filter { it.startsWith("scene_") && it.endsWith(".json") }.sorted()

    fun scenes(): List<Scene> = sceneFiles().mapNotNull {
        try { parseScene(open("scenes/$it").bufferedReader().readText()) } catch (e: Exception) { null }
    }

    fun speakers(): Map<String, Speaker> = try {
        val s = JSONObject(open("speakers.json").bufferedReader().readText()).getJSONObject("speakers")
        s.keys().asSequence().associateWith { k ->
            val o = s.getJSONObject(k)
            Speaker(k, o.getString("name"), if (o.isNull("avatar")) null else o.getString("avatar"), o.optString("letter").ifEmpty { null })
        }
    } catch (e: Exception) { emptyMap() }

    /** Картинки в JSON названы .png, в проекте лежат .webp. */
    fun bitmap(name: String): Bitmap? = try {
        open("art/" + name.substringBeforeLast('.') + ".webp").use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) { null }
}

object ContentStore {
    fun dirFor(ctx: Context) = File(ctx.filesDir, "content_sets")
    private fun pointer(ctx: Context) = File(ctx.filesDir, "content_current.txt")

    /** Лучший доступный набор: скачанный (если новее встроенного и целый) или встроенный. */
    fun load(ctx: Context): ContentSet {
        val builtIn = ContentSet(ctx, null)
        try {
            val name = pointer(ctx).takeIf { it.exists() }?.readText()?.trim()
            if (!name.isNullOrEmpty()) {
                val d = File(dirFor(ctx), name)
                if (d.isDirectory) {
                    val s = ContentSet(ctx, d)
                    if (s.version > builtIn.version && s.scenes().isNotEmpty()) return s
                }
            }
        } catch (e: Exception) { /* играем на встроенном */ }
        return builtIn
    }

    /** Атомарно переключить на новый набор и убрать старые. */
    fun activate(ctx: Context, name: String) {
        val tmp = File(ctx.filesDir, "content_current.tmp")
        tmp.writeText(name)
        if (!tmp.renameTo(pointer(ctx))) { pointer(ctx).delete(); tmp.renameTo(pointer(ctx)) }
        dirFor(ctx).listFiles()?.filter { it.name != name }?.forEach { it.deleteRecursively() }
    }
}
