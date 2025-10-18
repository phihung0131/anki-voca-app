package com.example.vocabqueue.vm

import android.content.Context
import com.example.vocabqueue.data.Prefs
import com.example.vocabqueue.data.WordRepository
import kotlinx.coroutines.flow.first

object AddWordHelper {
    suspend fun processAndQueue(context: Context, raw: String): String {
        val word = raw.trim()
        if (!word.matches(Regex("^[a-zA-Z\s-]+$"))) {
            return "Chỉ hỗ trợ từ tiếng Anh"
        }
        val prefs = Prefs(context)
        val server = prefs.serverUrl.first()
        val key = prefs.aiKey.first()

        if (server.isBlank() || key.isBlank()) {
            return "Chưa cấu hình Server URL / AI Key"
        }

        val repo = WordRepository(context)
        val exists = try { repo.checkWordExists(server, word) } catch (e: Exception) { false }
        return if (exists) {
            "Từ đã tồn tại trên DB"
        } else {
            repo.insertWord(word)
            "Đã thêm "$word" vào hàng đợi"
        }
    }
}
