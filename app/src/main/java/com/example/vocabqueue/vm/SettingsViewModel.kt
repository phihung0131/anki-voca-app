package com.example.vocabqueue.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabqueue.data.Prefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    val serverUrl: StateFlow<String> = prefs.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val aiKey: StateFlow<String> = prefs.aiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun save(server: String, key: String) = viewModelScope.launch {
        prefs.save(server, key)
    }
}
