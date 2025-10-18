package com.example.vocabqueue.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.vocabqueue.vm.AddWordHelper
import kotlinx.coroutines.launch

class AddWordActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selected = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()?.trim() ?: ""

        if (selected.isBlank()) {
            finish(); return
        }

        lifecycleScope.launch {
            val result = AddWordHelper.processAndQueue(applicationContext, selected)
            Toast.makeText(this@AddWordActivity, result, Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }
}
