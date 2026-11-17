package com.example.varinbloknot.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream

data class Speaker(val id: String, val name: String, val avatar: String?, val letter: String?)

/** Предмет магазина (сцены с бюджетом). */
data class DesignItem(val name: String, val price: Int?, val acc: String, val layers: List<String>)

/** Раскрашиваемый предмет. mode: "tint" — слой заменяется на <layer>_tint; "mask" — поверх слоя рисуется <overlay>. */
data class PaintObj(val id: String, val label: String, val layer: String, val mode: String, val overlay: String?)

data class Design(
    val hint: String, val palette: List<String>, val budget: Int?,
    val items: List<DesignItem>, val paint: List<PaintObj>,
    val shopHint: String, val paintHint: String, val brief: Brief? = null,
)

data class Variant(val cond: String, val text: String)
/** Вариант ответа игрока: своя ответная реплика (replyWho/replyText). cond — когда вариант показывать. */
data class Choice(val text: String, val cond: String, val replyWho: String, val replyText: String)
/** cond — условие показа реплики (см. Dialog.kt); variants — тексты на разные случаи. */
data class Line(
    val who: String, val text: String, val camera: String, val choices: List<Choice> = emptyList(),
    val cond: String = "", val variants: List<Variant> = emptyList(),
)
data class Brief(val who: String, val text: String)
data class LayerDef(val name: String, val x: Int, val y: Int, val w: Int, val h: Int, val tint: String?)
data class SceneArt(val width: Int, val height: Int, val layers: List<LayerDef>)
data class CharDef(val id: String, val file: String, val relHeight: Float, val w: Int, val h: Int)

/** Карточка игры «Нужно или хочется?». */
data class SortItem(val name: String, val bin: String, val why: String, val hint: String, val caption: String, val emoji: String)

data class Task(
    val type: String, val title: String, val question: String = "",
    val limit: Int = 0, val budgetItems: List<Pair<String, Int>> = emptyList(),
    val bins: List<String> = emptyList(), val sortItems: List<SortItem> = emptyList(),
    val options: List<String> = emptyList(), val correct: Int = 0, val explain: String = "", val hint: String = "",
    // type = "plan": задание из чисел игрока
    val priority: List<String> = emptyList(), val texts: Map<String, String> = emptyMap(),
)

data class Card(val subject: String, val title: String, val art: String, val text: String, val example: String)
data class Scene(
    val id: Int, val title: String, val tags: List<String>, val story: String,
    val dir: String, val color: String, val design: Design, val dialog: List<Line>,
    val task: Task, val card: Card, val cameraAreas: Map<String, Pair<Float, Float>>,
    val stage: Map<String, Pair<Float, Float>>,
)

private fun JSONArray.strings() = List(length()) { getString(it) }

private fun parseSortItem(a: JSONArray, i: Int): SortItem {
    val o = a.get(i)
    if (o is JSONObject) {
        return SortItem(o.getString("name"), o.getString("bin"), o.optString("why"), o.optString("hint"), o.optString("caption"), o.optString("emoji"))
    }
    val p = o as JSONArray
    return SortItem(p.getString(0), p.getString(1), p.optString(2), p.optString(3), p.optString(4), p.optString(5))
}

fun parseScene(text: String): Scene {
    val j = JSONObject(text)
    val d = j.getJSONObject("design")
    val items = d.optJSONArray("items")?.let { a ->
        List(a.length()) {
            val o = a.getJSONObject(it)
            DesignItem(o.getString("name"), if (o.isNull("price")) null else o.getInt("price"),
                o.optString("acc", o.getString("name").lowercase()), o.optJSONArray("layers")?.strings().orEmpty())
        }
    }.orEmpty()
    val paint = d.optJSONArray("paint")?.let { a ->
        List(a.length()) {
            val o = a.getJSONObject(it)
            PaintObj(o.getString("id"), o.getString("label"), o.getString("layer"), o.optString("mode", "tint"), o.optString("overlay").ifEmpty { null })
        }
    }.orEmpty()
    val design = Design(
        d.optString("hint"), d.getJSONArray("palette").strings(),
        if (d.isNull("budget")) null else d.optInt("budget"), items, paint,
        d.optString("shopHint", "У тебя {budget}. На всё не хватит — выбери самое нужное"),
        d.optString("paintHint", "Выбери карандаш и нажми на предмет"),
        d.optJSONObject("brief")?.let { Brief(it.getString("who"), it.getString("text")) },
    )
    val dl = j.getJSONArray("dialog").let { a ->
        List(a.length()) {
            val o = a.getJSONObject(it)
            val ch = o.optJSONArray("choices")?.let { c ->
                List(c.length()) { k ->
                    val x = c.get(k)
                    if (x is JSONObject) {
                        val r = x.optJSONObject("reply")
                        Choice(x.getString("text"), x.optString("if"), r?.optString("who").orEmpty(), r?.optString("text").orEmpty())
                    } else Choice(x.toString(), "", "", "")
                }
            }.orEmpty()
            val vr = o.optJSONArray("variants")?.let { v ->
                List(v.length()) { k -> v.getJSONObject(k).let { Variant(it.optString("if"), it.getString("text")) } }
            }.orEmpty()
            Line(o.getString("who"), o.optString("text"), o.optString("camera", "wide"), ch, o.optString("if"), vr)
        }
    }
    val t = j.getJSONObject("task")
    val type = t.getString("type")
    val task = when (type) {
        "budget" -> Task(type, t.getString("title"), limit = t.getInt("limit"),
            budgetItems = t.getJSONArray("items").let { a ->
                List(a.length()) { a.getJSONArray(it).let { p -> p.getString(0) to p.getInt(1) } }
            })
        "sort" -> Task(type, t.getString("title"), t.optString("question"), bins = t.getJSONArray("bins").strings(),
            sortItems = t.getJSONArray("items").let { a -> List(a.length()) { parseSortItem(a, it) } })
        "plan" -> Task(type, t.getString("title"), priority = t.getJSONArray("priority").strings(),
            texts = t.optJSONObject("texts")?.let { o -> o.keys().asSequence().associateWith { k -> o.getString(k) } }.orEmpty())
        else -> Task(type, t.getString("title"), t.optString("question"), options = t.getJSONArray("options").strings(),
            correct = t.getInt("correct"), explain = t.optString("explain"), hint = t.optString("hint"))
    }
    val c = j.getJSONObject("card")
    val card = Card(c.getString("subject"), c.getString("title"), c.getString("art"), c.getString("text"), c.getString("example"))
    val areas = HashMap<String, Pair<Float, Float>>()
    j.optJSONObject("cameraAreas")?.let { o ->
        o.keys().forEach { k -> o.getJSONObject(k).let { areas[k] = it.getDouble("x").toFloat() to it.getDouble("y").toFloat() } }
    }
    val stage = HashMap<String, Pair<Float, Float>>()
    j.optJSONObject("stage")?.let { o ->
        o.keys().forEach { k -> o.getJSONObject(k).let { stage[k] = it.getDouble("x").toFloat() to it.getDouble("y").toFloat() } }
    }
    val art = j.getJSONObject("art")
    return Scene(j.getInt("id"), j.getString("title"), j.getJSONArray("tags").strings(), j.getString("story"),
        art.getString("dir"), art.getString("color"), design, dl, task, card, areas, stage)
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

    fun sceneArt(dir: String): SceneArt? = try {
        val j = JSONObject(open("art/$dir/layers.json").bufferedReader().readText())
        val a = j.getJSONArray("layers")
        SceneArt(j.getInt("width"), j.getInt("height"), List(a.length()) {
            val o = a.getJSONObject(it)
            LayerDef(o.getString("name"), o.getInt("x"), o.getInt("y"), o.getInt("w"), o.getInt("h"), o.optString("tint").ifEmpty { null })
        })
    } catch (e: Exception) { null }

    fun characters(): Map<String, CharDef> = try {
        val a = JSONObject(open("art/characters/characters.json").bufferedReader().readText()).getJSONArray("characters")
        List(a.length()) {
            val o = a.getJSONObject(it)
            CharDef(o.getString("id"), o.getString("file"), o.getDouble("relHeight").toFloat(), o.getInt("w"), o.getInt("h"))
        }.associateBy { it.id }
    } catch (e: Exception) { emptyMap() }

    /** Картинки в JSON могут быть названы .png, в проекте лежат .webp. */
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
