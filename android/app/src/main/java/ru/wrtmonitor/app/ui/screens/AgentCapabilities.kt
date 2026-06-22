package ru.wrtmonitor.app.ui.screens

private val capabilityGroups = linkedMapOf(
    "Agent" to listOf("agent."),
    "Telemetry" to listOf("telemetry."),
    "Wi-Fi" to listOf("wifi."),
    "Network" to listOf("network."),
    "Diagnostics" to listOf("diagnostics."),
    "System" to listOf("system."),
)

internal fun capabilitiesSummary(capabilities: Map<String, Boolean>): String {
    if (capabilities.isEmpty()) return "Нет данных"
    val enabled = capabilities.count { it.value }
    val disabled = capabilities.size - enabled
    return "$enabled enabled / $disabled disabled"
}

internal fun groupedCapabilities(
    capabilities: Map<String, Boolean>,
): List<Pair<String, List<String>>> {
    if (capabilities.isEmpty()) return emptyList()
    val remaining = capabilities.toSortedMap().toMutableMap()
    val result = mutableListOf<Pair<String, List<String>>>()

    capabilityGroups.forEach { (title, prefixes) ->
        val items = remaining.keys.filter { key ->
            prefixes.any { prefix -> key.startsWith(prefix) }
        }
        if (items.isNotEmpty()) {
            result += title to items
            items.forEach { remaining.remove(it) }
        }
    }

    if (remaining.isNotEmpty()) {
        result += "Other" to remaining.keys.toList()
    }

    return result
}
