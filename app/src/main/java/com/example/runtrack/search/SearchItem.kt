package com.example.runtrack.search

data class SearchItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val type: SearchType,
    val avatarResId: Int? = null,
    var isActionDone: Boolean = false
)
