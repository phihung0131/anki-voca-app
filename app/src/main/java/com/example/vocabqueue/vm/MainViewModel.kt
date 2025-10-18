package com.example.vocabqueue.vm

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabqueue.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = WordRepository(app)
    private val prefs = Prefs(app)

    val queue = repo.queueFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val serverUrl: StateFlow<String> = prefs.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val aiKey: StateFlow<String> = prefs.aiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun removeFromQueue(word: String) = viewModelScope.launch { repo.removeWord(word) }

    fun generateAndUpload() = viewModelScope.launch {
        val server = serverUrl.value
        val key = aiKey.value
        val words = queue.value.map { it.word }
        if (server.isBlank() || key.isBlank() || words.isEmpty()) return@launch
        Toast.makeText(getApplication(), "Đang gen ${words.size} từ...", Toast.LENGTH_SHORT).show()
        val items = repo.generateCollocations(key, words)
        if (items.isEmpty()) {
            Toast.makeText(getApplication(), "Không nhận được kết quả từ AI", Toast.LENGTH_SHORT).show()
            return@launch
        }
        val ok = repo.addCollocations(server, items)
        if (ok) {
            repo.clearQueue()
            Toast.makeText(getApplication(), "Đã upload thành công!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Upload thất bại!", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportCsv() = viewModelScope.launch {
        val server = serverUrl.value
        if (server.isBlank()) return@launch
        // minimal fire-and-forget open in browser to trigger download
        // A more robust implementation would use DownloadManager
        Toast.makeText(getApplication(), "Mở trình duyệt tải CSV...", Toast.LENGTH_SHORT).show()
        // handled in UI by ACTION_VIEW or here via intent, but Compose UI already has button using vm function.
    }

    fun deleteAllRemote() = viewModelScope.launch {
        // Optional: implement backend endpoint call; skipped for brevity
        Toast.makeText(getApplication(), "Hãy xóa trên server nếu API có sẵn.", Toast.LENGTH_SHORT).show()
    }
}
