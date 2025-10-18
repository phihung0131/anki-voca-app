package com.example.vocabqueue.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vocabqueue.data.WordEntity
import com.example.vocabqueue.vm.MainViewModel

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    MainScreen(vm)
                }
            }
        }
    }
}

@Composable
fun MainScreen(vm: MainViewModel) {
    val ctx = LocalContext.current
    val queue by vm.queue.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState("")
    val aiKey by vm.aiKey.collectAsState("")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("VocabQueue") },
                actions = {
                    TextButton(onClick = {
                        ctx.startActivity(Intent(ctx, SettingsActivity::class.java))
                    }) { Text("Cài đặt") }
                })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            if (serverUrl.isEmpty() || aiKey.isEmpty()) {
                Text("⚠️ Chưa cấu hình Server URL / AI Key. Vào Cài đặt trước khi dùng.", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = queue.isNotEmpty(),
                    onClick = { vm.generateAndUpload() }) { Text("Gen collocations") }
                Button(onClick = { vm.deleteAllRemote() }) { Text("Xóa hết (remote)") }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.exportCsv() }) { Text("Xuất CSV") }
                Button(onClick = {
                    val mongo = "https://cloud.mongodb.com/v2/68f35c2cca30fe58cf653e8a#/metrics/replicaSet/68f35d2762b03e0ef01bbdf7/explorer/test/collocations/find"
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mongo)))
                }) { Text("Xem tất cả") }
            }
            Spacer(Modifier.height(16.dp))
            Text("Hàng đợi (${queue.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            if (queue.isEmpty()) {
                Text("Chưa có từ nào trong hàng đợi")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(queue) { w: WordEntity ->
                        ElevatedCard {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(w.word, Modifier.weight(1f))
                                TextButton(onClick = { vm.removeFromQueue(w.word) }) { Text("Xóa") }
                            }
                        }
                    }
                }
            }
        }
    }
}
