package com.example.vocabqueue.data

fun String.find(ch: Char): Int { return this.indexOf(ch) }
fun String.rfind(ch: Char): Int { return this.lastIndexOf(ch) }
