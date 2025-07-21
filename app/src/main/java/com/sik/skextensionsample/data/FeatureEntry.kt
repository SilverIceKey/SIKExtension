package com.sik.skextensionsample.data

data class FeatureEntry(
    val title: String,
    val desc: String,
    val children: List<FeatureEntry> = emptyList(),
    val action: () -> Unit = {}
)
