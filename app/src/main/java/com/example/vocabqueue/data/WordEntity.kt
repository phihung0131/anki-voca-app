package com.example.vocabqueue.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_queue")
data class WordEntity(
    @PrimaryKey val word: String
)
