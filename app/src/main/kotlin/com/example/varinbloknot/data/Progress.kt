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
}
