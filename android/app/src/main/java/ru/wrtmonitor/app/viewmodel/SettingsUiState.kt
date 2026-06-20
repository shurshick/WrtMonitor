package ru.wrtmonitor.app.viewmodel

data class SettingsUiState(
    val serverUrl: String = "",
    val saving: Boolean = false,
    val error: String? = null,
)
