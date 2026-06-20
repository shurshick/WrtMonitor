package ru.wrtmonitor.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import ru.wrtmonitor.app.R
import ru.wrtmonitor.app.api.ApiResult
import ru.wrtmonitor.app.api.WrtMonitorApi
import ru.wrtmonitor.app.api.dto.DeviceDto
import ru.wrtmonitor.app.ui.components.InfoRow
import ru.wrtmonitor.app.viewmodel.DeviceDetailUiState

@Composable
fun DeviceDetailScreen(serverUrl: String, accessToken: String, device: DeviceDto) {
    val scope = rememberCoroutineScope(); var state by remember(device.id) { mutableStateOf(DeviceDetailUiState(loading = true, device = device)) }
    fun refresh() { state = state.copy(loading = true, error = null); scope.launch { when (val result = withContext(Dispatchers.IO) { WrtMonitorApi(serverUrl, accessToken).getLatestTelemetry(device.id) }) { is ApiResult.Success -> state = state.copy(loading = false, telemetry = result.data); is ApiResult.Error -> state = state.copy(loading = false, error = result.message) } } }
    LaunchedEffect(serverUrl, accessToken, device.id) { refresh() }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(device.name.ifBlank { device.hostname }, style = MaterialTheme.typography.titleLarge); InfoRow(stringResource(R.string.model), device.model, stringResource(R.string.no_data)); InfoRow(stringResource(R.string.firmware), device.firmware, stringResource(R.string.no_data)) } }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.telemetry), style = MaterialTheme.typography.titleLarge); Button({ refresh() }, enabled = !state.loading) { Text(stringResource(R.string.refresh)) } }
        when { state.loading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; state.error != null -> Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error); state.telemetry == null -> Text(stringResource(R.string.no_data)); else -> TelemetrySummary(state.telemetry!!) }
    }
}

@Composable
private fun TelemetrySummary(telemetry: ru.wrtmonitor.app.api.dto.TelemetryDto) {
    val payload = telemetry.payload; val system = payload?.optJSONObject("system"); val memory = system?.optJSONObject("memory")
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { InfoRow(stringResource(R.string.source), telemetry.source, stringResource(R.string.no_data)); InfoRow(stringResource(R.string.uptime), system?.optString("uptime"), stringResource(R.string.no_data)); InfoRow(stringResource(R.string.load), system?.optString("load"), stringResource(R.string.no_data)); InfoRow(stringResource(R.string.memory), memory?.let { "${it.optLong("available_kb") / 1024} / ${it.optLong("total_kb") / 1024} MB" }, stringResource(R.string.no_data)) } }
}
