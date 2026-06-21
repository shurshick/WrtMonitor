package ru.wrtmonitor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import ru.wrtmonitor.app.R
import ru.wrtmonitor.app.api.ApiResult
import ru.wrtmonitor.app.api.WrtMonitorApi
import ru.wrtmonitor.app.api.dto.DeviceDto
import ru.wrtmonitor.app.api.dto.TelemetryDto
import ru.wrtmonitor.app.api.isUnauthorized
import ru.wrtmonitor.app.ui.components.InfoRow
import ru.wrtmonitor.app.viewmodel.DeviceDetailUiState

@Composable
fun DeviceDetailScreen(
    serverUrl: String,
    accessToken: String,
    device: DeviceDto,
    onSessionExpired: () -> Unit,
    onArchived: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var state by remember(device.id) {
        mutableStateOf(DeviceDetailUiState(loading = true, device = device))
    }
    var actionMessage by remember(device.id) { mutableStateOf("") }
    var actionError by remember(device.id) { mutableStateOf("") }
    var confirmRollback by remember(device.id) { mutableStateOf(false) }
    var confirmArchive by remember(device.id) { mutableStateOf(false) }

    fun refresh() {
        state = state.copy(loading = true, error = null)
        actionError = ""
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                WrtMonitorApi(serverUrl, accessToken).getLatestTelemetry(device.id)
            }) {
                is ApiResult.Success -> state = state.copy(loading = false, telemetry = result.data)
                is ApiResult.Error -> {
                    if (result.isUnauthorized()) onSessionExpired()
                    else state = state.copy(loading = false, error = result.message)
                }
            }
        }
    }

    fun queueCommand(type: String, payload: JSONObject = JSONObject(), success: String) {
        actionMessage = ""
        actionError = ""
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                WrtMonitorApi(serverUrl, accessToken).createCommand(device.id, type, payload)
            }) {
                is ApiResult.Success -> {
                    actionMessage = success
                    refresh()
                }
                is ApiResult.Error -> {
                    if (result.isUnauthorized()) onSessionExpired() else actionError = result.message
                }
            }
        }
    }

    fun archiveDevice() {
        actionMessage = ""
        actionError = ""
        scope.launch {
            when (val result = withContext(Dispatchers.IO) {
                WrtMonitorApi(serverUrl, accessToken).archiveDevice(device.id)
            }) {
                is ApiResult.Success -> onArchived()
                is ApiResult.Error -> {
                    if (result.isUnauthorized()) onSessionExpired() else actionError = result.message
                }
            }
        }
    }

    LaunchedEffect(serverUrl, accessToken, device.id) { refresh() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(device.name.ifBlank { device.hostname }, style = MaterialTheme.typography.titleLarge)
                InfoRow(stringResource(R.string.model), device.model, stringResource(R.string.no_data))
                InfoRow(stringResource(R.string.firmware), device.firmware, stringResource(R.string.no_data))
                InfoRow(stringResource(R.string.status), device.status, stringResource(R.string.no_data))
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.telemetry), style = MaterialTheme.typography.titleLarge)
            Button({ refresh() }, enabled = !state.loading) { Text(stringResource(R.string.refresh)) }
        }
        when {
            state.loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
            state.telemetry == null -> Text(stringResource(R.string.no_data))
            else -> TelemetrySummary(state.telemetry!!)
        }
        AgentSection(
            telemetry = state.telemetry,
            actionMessage = actionMessage,
            actionError = actionError,
            canArchive = device.status == "disabled",
            onCheckUpdate = { queueCommand("agent.update", success = "Команда проверки обновления добавлена") },
            onEnableAutoUpdate = { queueCommand("agent.set_auto_update", JSONObject().put("enabled", true), "Auto-update будет включен при следующем опросе агента") },
            onDisableAutoUpdate = { queueCommand("agent.set_auto_update", JSONObject().put("enabled", false), "Auto-update будет выключен при следующем опросе агента") },
            onRollback = { confirmRollback = true },
            onArchive = { confirmArchive = true },
        )
    }

    if (confirmRollback) {
        AlertDialog(
            onDismissRequest = { confirmRollback = false },
            title = { Text("Rollback агента?") },
            text = { Text("Агент попробует восстановить предыдущую рабочую версию и перезапуститься.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRollback = false
                    queueCommand("agent.rollback", success = "Команда rollback добавлена")
                }) { Text("Rollback") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRollback = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (confirmArchive) {
        AlertDialog(
            onDismissRequest = { confirmArchive = false },
            title = { Text("Удалить из списка?") },
            text = { Text("Этот роутер уже отключён. История telemetry и команд останется на сервере, но для повторного подключения агент нужно будет зарегистрировать заново.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmArchive = false
                    archiveDevice()
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { confirmArchive = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun TelemetrySummary(telemetry: TelemetryDto) {
    val payload = telemetry.payload ?: return
    val system = payload.optJSONObject("system")
    val memory = system?.optJSONObject("memory")
    val cpu = payload.optJSONObject("cpu")
    val storage = payload.optJSONObject("storage")
    val thermal = payload.optJSONObject("thermal")
    val traffic = payload.optJSONObject("traffic")
    val processes = system?.optJSONObject("processes")
    val board = payload.optJSONObject("board")
    val release = board?.optJSONObject("release")
    val network = payload.optJSONObject("network")
    val networkDevices = payload.optJSONObject("network_devices")
    val interfaces = network?.optJSONArray("interface") ?: network?.optJSONArray("interfaces")
    val wifi = payload.optJSONObject("wifi")
    val radios = wifi?.optJSONArray("radios")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TelemetrySection("Состояние") {
            InfoRow(stringResource(R.string.updated_at), formatTimestamp(telemetry.createdAt), stringResource(R.string.no_data))
            InfoRow(stringResource(R.string.age), telemetry.ageSeconds?.let { "$it сек" }, stringResource(R.string.no_data))
            InfoRow(stringResource(R.string.source), telemetry.source, stringResource(R.string.no_data))
            if (telemetry.isStale) {
                Text(stringResource(R.string.stale_telemetry), color = MaterialTheme.colorScheme.error)
            }
        }
        TelemetrySection("Система") {
            InfoRow(stringResource(R.string.uptime), formatDuration(system?.optLong("uptime", 0) ?: 0))
            InfoRow(stringResource(R.string.load), system?.optString("load"), stringResource(R.string.no_data))
            InfoRow(stringResource(R.string.memory), memory?.let { memoryLabel(it) }, stringResource(R.string.no_data))
            InfoRow("Процессор", cpu?.optString("model").orEmpty().ifBlank { "Не определён" })
            InfoRow("Ядра CPU", cpu?.optLong("cores", 0)?.takeIf { it > 0 }?.toString(), stringResource(R.string.no_data))
            InfoRow("Накопитель", storage?.let { storageLabel(it) }, stringResource(R.string.no_data))
            InfoRow("Температура", thermalLabel(thermal), stringResource(R.string.no_data))
            InfoRow("Процессы", processes?.optLong("count", 0)?.takeIf { it > 0 }?.toString(), stringResource(R.string.no_data))
        }
        TelemetrySection("Оборудование") {
            InfoRow(stringResource(R.string.model), board?.optString("model").orEmpty().ifBlank { null }, stringResource(R.string.no_data))
            InfoRow(stringResource(R.string.firmware), release?.optString("description").orEmpty().ifBlank { release?.optString("version") }, stringResource(R.string.no_data))
        }
        TelemetrySection("Сеть") {
            InfoRow("RX / TX", traffic?.let { "${formatBytes(it.optLong("rx_bytes"))} / ${formatBytes(it.optLong("tx_bytes"))}" }, stringResource(R.string.no_data))
            if (interfaces == null || interfaces.length() == 0) Text("Агент ещё не передал интерфейсы") else InterfaceRows(interfaces)
            if (networkDevices != null) NetworkDeviceRows(networkDevices)
        }
        TelemetrySection("Wi-Fi") {
            if (wifi?.optBoolean("available", false) != true) Text(stringResource(R.string.wifi_unavailable)) else RadioRows(radios)
        }
    }
}

@Composable
private fun AgentSection(
    telemetry: TelemetryDto?,
    actionMessage: String,
    actionError: String,
    canArchive: Boolean,
    onCheckUpdate: () -> Unit,
    onEnableAutoUpdate: () -> Unit,
    onDisableAutoUpdate: () -> Unit,
    onRollback: () -> Unit,
    onArchive: () -> Unit,
) {
    val agent = telemetry?.payload?.optJSONObject("agent")
    val autoUpdateEnabled = agent?.optBoolean("auto_update_enabled", true) == true
    TelemetrySection("Agent") {
        InfoRow("Версия", agent?.optString("version"), stringResource(R.string.no_data))
        InfoRow("Auto-update", if (agent == null) null else if (autoUpdateEnabled) "Включено" else "Выключено", stringResource(R.string.no_data))
        InfoRow("Доступная версия", agent?.optString("available_version"), stringResource(R.string.no_data))
        InfoRow("Последняя проверка", formatTimestamp(agent?.optString("last_update_check")), stringResource(R.string.no_data))
        InfoRow("Статус обновления", agent?.optString("last_update_status"), stringResource(R.string.no_data))
        InfoRow("Последнее успешное обновление", formatTimestamp(agent?.optString("last_successful_update")), stringResource(R.string.no_data))
        InfoRow("Последняя ошибка", agent?.optString("last_update_error"), stringResource(R.string.no_data))
        InfoRow("Backup", if (agent == null) null else if (agent.optBoolean("backup_available", false)) "Доступен" else "Нет", stringResource(R.string.no_data))
        InfoRow("Источник", agent?.optString("update_source"), stringResource(R.string.no_data))
        if (actionMessage.isNotBlank()) Text(actionMessage, color = MaterialTheme.colorScheme.primary)
        if (actionError.isNotBlank()) Text(actionError, color = MaterialTheme.colorScheme.error)
        Button(onClick = onCheckUpdate, modifier = Modifier.fillMaxWidth()) { Text("Проверить обновление") }
        Button(
            onClick = if (autoUpdateEnabled) onDisableAutoUpdate else onEnableAutoUpdate,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (autoUpdateEnabled) "Выключить auto-update" else "Включить auto-update") }
        Button(onClick = onRollback, modifier = Modifier.fillMaxWidth()) { Text("Rollback agent") }
        if (canArchive) {
            TextButton(onClick = onArchive, modifier = Modifier.fillMaxWidth()) {
                Text("Удалить из списка", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TelemetrySection(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun InterfaceRows(interfaces: JSONArray) {
    for (index in 0 until interfaces.length()) {
        val item = interfaces.optJSONObject(index) ?: continue
        val name = item.optString("interface", item.optString("name", "interface"))
        val state = if (item.optBoolean("up", false)) "В сети" else "Не в сети"
        val address = firstAddress(item.optJSONArray("ipv4-address"))
        InfoRow(name, listOf(state, address).filter { it.isNotBlank() }.joinToString(" · "))
    }
}

@Composable
private fun RadioRows(radios: JSONArray?) {
    if (radios == null || radios.length() == 0) {
        Text(stringResource(R.string.wifi_unavailable))
        return
    }
    for (index in 0 until radios.length()) {
        val radio = radios.optJSONObject(index) ?: continue
        val name = radio.optString("name", "radio$index")
        val ssid = radio.optJSONArray("ssid")?.optString(0).orEmpty()
        val details = listOf(
            if (radio.optBoolean("up", false)) "Включён" else "Выключен",
            ssid,
            radio.optString("band"),
            radio.optString("channel"),
        ).filter { it.isNotBlank() }.joinToString(" · ")
        InfoRow(name, details)
    }
}

@Composable
private fun NetworkDeviceRows(devices: JSONObject) {
    val names = devices.keys().asSequence().toList().sorted()
    for (name in names) {
        val item = devices.optJSONObject(name) ?: continue
        val details = listOf(
            if (item.optBoolean("up", false)) "Активен" else "Неактивен",
            if (item.optBoolean("carrier", false)) "carrier есть" else "carrier нет",
            item.optLong("mtu", 0).takeIf { it > 0 }?.let { "MTU $it" }.orEmpty(),
        ).filter { it.isNotBlank() }.joinToString(" · ")
        InfoRow(name, details)
    }
}

private fun firstAddress(addresses: JSONArray?): String =
    addresses?.optJSONObject(0)?.optString("address").orEmpty()

private fun memoryLabel(memory: JSONObject): String =
    "${memory.optLong("available_kb") / 1024} / ${memory.optLong("total_kb") / 1024} MB"

private fun storageLabel(storage: JSONObject): String =
    "${storage.optLong("used_kb") / 1024} использовано, ${storage.optLong("available_kb") / 1024} MB свободно"

private fun thermalLabel(thermal: JSONObject?): String? =
    if (thermal?.optBoolean("available", false) == true) {
        "${thermal.optLong("milli_celsius") / 1000.0} °C"
    } else {
        null
    }

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private fun formatTimestamp(value: String?): String? = runCatching {
    if (value.isNullOrBlank()) null else OffsetDateTime.parse(value)
        .atZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))
}.getOrNull()

private fun formatDuration(seconds: Long): String {
    val days = seconds / 86_400
    val hours = (seconds % 86_400) / 3_600
    val minutes = (seconds % 3_600) / 60
    return listOfNotNull(
        days.takeIf { it > 0 }?.let { "$it д" },
        hours.takeIf { it > 0 }?.let { "$it ч" },
        minutes.let { "$it мин" },
    ).joinToString(" ")
}
