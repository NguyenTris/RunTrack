package com.example.runtrack.settings

data class FaqItem(
    val id: String,
    val category: String,
    val question: String,
    val answer: String,
    var isExpanded: Boolean = false
)
