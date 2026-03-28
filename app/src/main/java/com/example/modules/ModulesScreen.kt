package com.example.modules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModulesScreen(
    state: AppState,
    onBaseUrlChange: (String) -> Unit,
    onManagementKeyChange: (String) -> Unit,
    onRefreshAll: () -> Unit,
    onRefreshModule: (ServiceKind) -> Unit,
    onStartModule: (ServiceModule) -> Unit,
    onStopModule: (ServiceModule) -> Unit,
    onRestartModule: (ServiceModule) -> Unit,
    onOpenModule: (ServiceModule) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Modules") }) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ConfigCard(
                    settings = state.settings,
                    refreshing = state.refreshing,
                    onBaseUrlChange = onBaseUrlChange,
                    onManagementKeyChange = onManagementKeyChange,
                    onRefreshAll = onRefreshAll,
                )
            }
            items(state.modules) { module ->
                ModuleCard(
                    module = module,
                    onRefresh = { onRefreshModule(module.kind) },
                    onOpen = { onOpenModule(module) },
                    onStart = { onStartModule(module) },
                    onStop = { onStopModule(module) },
                    onRestart = { onRestartModule(module) },
                )
            }
        }
    }
}

@Composable
private fun ConfigCard(
    settings: CliProxySettings,
    refreshing: Boolean,
    onBaseUrlChange: (String) -> Unit,
    onManagementKeyChange: (String) -> Unit,
    onRefreshAll: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Cấu hình monitor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = settings.baseUrl,
                onValueChange = onBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("CLIProxyAPI base URL") },
                singleLine = true,
            )
            OutlinedTextField(
                value = settings.managementKey,
                onValueChange = onManagementKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Management key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRefreshAll) {
                    Text("Refresh all")
                }
                if (refreshing) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ModuleCard(
    module: ServiceModule,
    onRefresh: () -> Unit,
    onOpen: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
) {
    val containerColor = if (module.isRunning) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(module.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Session: ${module.sessionName}")
            Text("Start: ${module.startCommand}")
            module.endpoint?.let { Text("Endpoint: $it") }
            Text("Trạng thái: ${module.status}")
            module.details?.takeIf { it.isNotBlank() }?.let { Text("Chi tiết: $it") }
            if (module.lastUpdatedLabel.isNotBlank()) {
                Text("Cập nhật lúc: ${module.lastUpdatedLabel}")
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                module.notes.distinct().forEach { note ->
                    Text("• $note")
                }
            }
            if (module.logs.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("Logs gần nhất", fontWeight = FontWeight.Bold)
                        module.logs.take(8).forEach { line ->
                            Text(line, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRefresh) { Text("Refresh") }
                    Button(onClick = onOpen) { Text("Open") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onStart) { Text("Start") }
                    Button(onClick = onStop) { Text("Stop") }
                    Button(onClick = onRestart) { Text("Restart") }
                }
            }
        }
    }
}

fun openModule(context: Context, module: ServiceModule) {
    when (module.kind) {
        ServiceKind.CLI_PROXY_API -> {
            val endpoint = module.endpoint ?: return
            val uri = Uri.parse("$endpoint/management.html")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            runCatching {
                context.startActivity(intent)
            }.onFailure {
                Toast.makeText(context, "Không mở được dashboard", Toast.LENGTH_SHORT).show()
            }
        }
        ServiceKind.OPENCLAW -> TermuxBridge.openTermuxApp(context)
    }
}
