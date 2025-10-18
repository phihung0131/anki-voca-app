package com.example.vocabqueue.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class CheckWordResponse(val exists: Boolean?)
data class CollocationItem(val collocation: String, val ipa: String, val meaning: String, val synonyms: String?)
data class AddCollocationsResponse(val status: String?, val message: String?)

class WordRepository(private val context: Context) {
    private val db = AppDatabase.get(context)
    private val dao = db.wordDao()
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    fun queueFlow() = dao.getAll()

    suspend fun insertWord(w: String) = dao.insert(WordEntity(w))
    suspend fun removeWord(w: String) = dao.delete(w)
    suspend fun clearQueue() = dao.clear()

    suspend fun checkWordExists(serverUrl: String, word: String): Boolean = withContext(Dispatchers.IO) {
        val url = serverUrl.trimEnd('/') + "/api/check-word"
        val body = gson.toJson(mapOf("word" to word)).toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(url).post(body).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext false
            val obj = gson.fromJson(resp.body!!.charStream(), CheckWordResponse::class.java)
            obj.exists == true
        }
    }

    suspend fun addCollocations(serverUrl: String, items: List<CollocationItem>): Boolean = withContext(Dispatchers.IO) {
        val url = serverUrl.trimEnd('/') + "/api/add-collocations"
        val body = gson.toJson(mapOf("collocations" to items)).toRequestBody("application/json".toMediaType())
        val req = Request.Builder().url(url).post(body).build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext false
            val obj = gson.fromJson(resp.body!!.charStream(), AddCollocationsResponse::class.java)
            obj.status == "success"
        }
    }

    suspend fun generateCollocations(apiKey: String, words: List<String>): List<CollocationItem> = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(words)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=$apiKey"
        val payload = gson.toJson(mapOf(
            "contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))),
            "generationConfig" to mapOf("temperature" to 0.7, "maxOutputTokens" to 2000, "topK" to 40, "topP" to 0.95)
        ))
        val req = Request.Builder()
            .url(url)
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext emptyList()
            val bodyStr = resp.body!!.string()
            // naive extract JSON block
            val jsonStart = bodyStr.find('{')
            val jsonEnd = bodyStr.rfind('}')
            if (jsonStart == -1 or jsonEnd == -1) return@withContext emptyList()
            val json = bodyStr[jsonStart:jsonEnd+1]
            try {
                val root = gson.fromJson(json, Map::class.java)
                val results = (root["results"] as? List<Map<String, Any?>>) ?: emptyList()
                results.mapNotNull { m ->
                    val coll = m["collocation"] as? String ?: return@mapNotNull null
                    CollocationItem(
                        collocation = coll,
                        ipa = (m["ipa"] as? String) ?: "",
                        meaning = (m["meaning"] as? String) ?: "",
                        synonyms = (m["synonyms"] as? String)
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun buildPrompt(words: List<String>) = """
Tạo collocations cho danh sách từ sau: ${words.joinToString(", ") { ""${it}"" }}

Yêu cầu chi tiết:
1. Xử lý từng từ:
   - Nếu là động từ/danh từ không ở dạng nguyên mẫu, chuyển về dạng nguyên mẫu
   - Nếu là một collocation, giữ nguyên
2. Với mỗi từ/cụm từ, tạo 1-5 collocations phổ biến; nghĩa Việt ngắn gọn; IPA chuẩn; synonyms nếu có
3. Trả về JSON đúng cấu trúc:
{
  "results": [ { "collocation": "strong coffee", "ipa": "/strɒŋ ˈkɒfi/", "meaning": "cà phê đậm đà", "synonyms": "..." } ]
}
4. Chỉ trả JSON, không kèm giải thích.
""".trimIndent()
}
