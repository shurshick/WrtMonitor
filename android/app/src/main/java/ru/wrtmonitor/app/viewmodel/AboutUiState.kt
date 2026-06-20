package ru.wrtmonitor.app.viewmodel

data class AboutUiState(
    val checking: Boolean = false,
    val latestVersion: String? = null,
    val releaseUrl: String? = null,
    val error: String? = null,
)
