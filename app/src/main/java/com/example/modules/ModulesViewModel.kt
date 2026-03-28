package com.example.modules

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ModulesViewModel(
    private val settingsStore: SettingsStore,
    private val cliProxyApiClient: CliProxyApiClient,
) : ViewModel() {
    var state by mutableStateOf(AppState(settings = settingsStore.load()))
        private set

    init {
        refreshAll()
        viewModelScope.launch {
            while (isActive) {
                delay(10_000)
                refreshAll(silent = true)
            }
        }
    }

    fun updateBaseUrl(value: String) {
        val settings = state.settings.copy(baseUrl = value)
        settingsStore.save(settings)
        state = state.copy(settings = settings)
    }

    fun updateManagementKey(value: String) {
        val settings = state.settings.copy(managementKey = value)
        settingsStore.save(settings)
        state = state.copy(settings = settings)
    }

    fun refreshAll(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                state = state.copy(refreshing = true)
            }
            val snapshot = cliProxyApiClient.fetchSnapshot(state.settings)
            state = state.copy(
                modules = state.modules.map { module ->
                    when (module.kind) {
                        ServiceKind.CLI_PROXY_API -> module.copy(
                            endpoint = snapshot.endpoint.ifBlank { module.endpoint },
                            isRunning = snapshot.ok,
                            status = snapshot.status,
                            details = snapshot.details,
                            logs = snapshot.logs,
                            notes = module.notes + snapshot.configSummary,
                            lastUpdatedLabel = nowLabel(),
                        )
                        ServiceKind.OPENCLAW -> module.copy(
                            isRunning = false,
                            status = "Chưa kiểm tra được từ app",
                            details = "Mở Termux và kiểm tra session: ${module.sessionName}",
                            lastUpdatedLabel = nowLabel(),
                        )
                    }
                },
                refreshing = false,
            )
        }
    }

    fun refreshModule(kind: ServiceKind) {
        viewModelScope.launch {
            val snapshot = cliProxyApiClient.fetchSnapshot(state.settings)
            state = state.copy(
                modules = state.modules.map { module ->
                    if (module.kind != kind) return@map module
                    when (kind) {
                        ServiceKind.CLI_PROXY_API -> module.copy(
                            endpoint = snapshot.endpoint.ifBlank { module.endpoint },
                            isRunning = snapshot.ok,
                            status = snapshot.status,
                            details = snapshot.details,
                            logs = snapshot.logs,
                            notes = defaultModules().first { it.kind == kind }.notes + snapshot.configSummary,
                            lastUpdatedLabel = nowLabel(),
                        )
                        ServiceKind.OPENCLAW -> module.copy(
                            isRunning = false,
                            status = "Chưa kiểm tra được từ app",
                            details = "Mở Termux và kiểm tra session: ${module.sessionName}",
                            lastUpdatedLabel = nowLabel(),
                        )
                    }
                },
            )
        }
    }

    private fun nowLabel(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }
}

class ModulesViewModelFactory(
    private val settingsStore: SettingsStore,
    private val cliProxyApiClient: CliProxyApiClient,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModulesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ModulesViewModel(settingsStore, cliProxyApiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
