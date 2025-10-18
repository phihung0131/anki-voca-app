package com.example.vocabqueue.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("vocab_prefs")

object PrefKeys {
    val SERVER_URL = stringPreferencesKey("server_url")
    val AI_KEY = stringPreferencesKey("ai_key")
}

class Prefs(private val context: Context) {
    val serverUrl: Flow<String> = context.dataStore.data.map { it[PrefKeys.SERVER_URL] ?: "" }
    val aiKey: Flow<String> = context.dataStore.data.map { it[PrefKeys.AI_KEY] ?: "" }

    suspend fun save(server: String, key: String) {
        context.dataStore.edit { prefs ->
            prefs[PrefKeys.SERVER_URL] = server
            prefs[PrefKeys.AI_KEY] = key
        }
    }
}
