package com.example.varinbloknot.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("progress")

class Progress(private val ctx: Context) {
    private val doneKey = stringSetPreferencesKey("done_scenes")
    private val cardsKey = stringSetPreferencesKey("cards")
    private val lastContentCheck = longPreferencesKey("last_content_check")
    private val lastAppCheck = longPreferencesKey("last_app_check")

    /** Сцены, пройденные до конца. */
    val done: Flow<Set<Int>> = ctx.store.data.map { p -> p[doneKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet() }

    /** Карточки, которые лежат в блокноте (по номеру сцены). */
    val cards: Flow<Set<Int>> = ctx.store.data.map { p -> p[cardsKey].orEmpty().mapNotNull { it.toIntOrNull() }.toSet() }

    suspend fun markDone(sceneId: Int) = ctx.store.edit { it[doneKey] = it[doneKey].orEmpty() + sceneId.toString() }
    suspend fun addCard(sceneId: Int) = ctx.store.edit { it[cardsKey] = it[cardsKey].orEmpty() + sceneId.toString() }

    suspend fun contentCheckedAt(): Long = ctx.store.data.first()[lastContentCheck] ?: 0L
    suspend fun appCheckedAt(): Long = ctx.store.data.first()[lastAppCheck] ?: 0L
    suspend fun setContentChecked(t: Long) = ctx.store.edit { it[lastContentCheck] = t }
    suspend fun setAppChecked(t: Long) = ctx.store.edit { it[lastAppCheck] = t }

    /** Что игрок успел сделать в сцене: шаг, покупки, цвета, выбранные ответы. */
    suspend fun loadScene(id: Int): SceneProgress? =
        ctx.store.data.first()[stringPreferencesKey("scene_$id")]?.let { SceneProgress.fromJson(it) }

    suspend fun saveScene(id: Int, p: SceneProgress) = ctx.store.edit { it[stringPreferencesKey("scene_$id")] = p.toJson() }
    suspend fun clearScene(id: Int) = ctx.store.edit { it.remove(stringPreferencesKey("scene_$id")) }

    /** Тест: начать одну сцену заново (прогресс, «пройдено», карточка). */
    suspend fun resetScene(id: Int) = ctx.store.edit {
        it.remove(stringPreferencesKey("scene_$id"))
        it[doneKey] = it[doneKey].orEmpty() - id.toString()
        it[cardsKey] = it[cardsKey].orEmpty() - id.toString()
    }

    /** Тест: сбросить всё прохождение (проверки обновлений не трогаем). */
    suspend fun resetAll() = ctx.store.edit { p ->
        p.asMap().keys.filter { it.name == "done_scenes" || it.name == "cards" || it.name.startsWith("scene_") }.forEach { p.remove(it) }
    }
}

class SceneProgress(
    val step: Int, val sub: String, val bought: List<String>, val tints: Map<String, Int>, val picks: Map<Int, String>,
) {
    fun toJson(): String = org.json.JSONObject().apply {
        put("step", step); put("sub", sub)
        put("bought", org.json.JSONArray(bought))
        put("tints", org.json.JSONObject().also { o -> tints.forEach { (k, v) -> o.put(k, v) } })
        put("picks", org.json.JSONObject().also { o -> picks.forEach { (k, v) -> o.put(k.toString(), v) } })
    }.toString()

    companion object {
        fun fromJson(s: String): SceneProgress? = try {
            val j = org.json.JSONObject(s)
            val b = j.optJSONArray("bought")
            val t = j.optJSONObject("tints")
            val p = j.optJSONObject("picks")
            SceneProgress(
                j.optInt("step"), j.optString("sub", "shop"),
                List(b?.length() ?: 0) { b!!.getString(it) },
                t?.keys()?.asSequence()?.associateWith { k -> t.getInt(k) }.orEmpty(),
                p?.keys()?.asSequence()?.associate { k -> k.toInt() to p.getString(k) }.orEmpty(),
            )
        } catch (e: Exception) { null }
    }
}
