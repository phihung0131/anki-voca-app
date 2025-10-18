package com.example.vocabqueue.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vocabqueue.vm.SettingsViewModel

class SettingsActivity : ComponentActivity() {
    private val vm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    SettingsScreen(vm, onClose = { finish() })
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel, onClose: () -> Unit) {
    val server by vm.serverUrl.collectAsState("")
    val key by vm.aiKey.collectAsState("")
    var s by remember { mutableStateOf(server) }
    var k by remember { mutableStateOf(key) }

    Scaffold(topBar = { TopAppBar(title = { Text("Cài đặt") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = s, onValueChange = { s = it }, label = { Text("Server URL (https://...)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = k, onValueChange = { k = it }, label = { Text("AI API Key (Gemini)") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.save(s, k); onClose() }) { Text("Lưu") }
                OutlinedButton(onClick = onClose) { Text("Hủy") }
            }
        }
    }
}
