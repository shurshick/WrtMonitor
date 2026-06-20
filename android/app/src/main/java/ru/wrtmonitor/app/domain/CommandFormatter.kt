package ru.wrtmonitor.app.domain

object CommandFormatter {
    fun statusLabel(status: String): String = when (status.lowercase()) {
        "queued" -> "В очереди"
        "sent" -> "Отправлена агенту"
        "success", "done" -> "Выполнена"
        "failed" -> "Ошибка"
        "expired" -> "Истекла"
        else -> status
    }
}
