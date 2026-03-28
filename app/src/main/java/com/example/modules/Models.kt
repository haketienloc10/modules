package com.example.modules

enum class ServiceKind {
    CLI_PROXY_API,
    OPENCLAW,
}

data class CliProxySettings(
    val baseUrl: String = "http://127.0.0.1:8317",
    val managementKey: String = "",
)

data class ServiceModule(
    val kind: ServiceKind,
    val title: String,
    val sessionName: String,
    val startCommand: String,
    val stopCommand: String,
    val endpoint: String? = null,
    val status: String,
    val isRunning: Boolean = false,
    val details: String? = null,
    val notes: List<String>,
    val logs: List<String> = emptyList(),
    val lastUpdatedLabel: String = "",
)

data class CliProxySnapshot(
    val ok: Boolean,
    val status: String,
    val details: String,
    val endpoint: String,
    val logs: List<String>,
    val configSummary: List<String>,
)

data class AppState(
    val settings: CliProxySettings = CliProxySettings(),
    val modules: List<ServiceModule> = defaultModules(),
    val refreshing: Boolean = false,
)

fun defaultModules(): List<ServiceModule> = listOf(
    ServiceModule(
        kind = ServiceKind.CLI_PROXY_API,
        title = "CLIProxyAPI",
        sessionName = "cliproxyapi-session",
        startCommand = "tmux has-session -t cliproxyapi-session 2>/dev/null || tmux new-session -d -s cliproxyapi-session 'clip-proxy-api'",
        stopCommand = "tmux kill-session -t cliproxyapi-session 2>/dev/null || true",
        endpoint = "http://127.0.0.1:8317",
        status = "Đang chờ kiểm tra",
        notes = listOf(
            "Session tmux: cliproxyapi-session",
            "Health: /v0/management/config",
            "Logs: /v0/management/logs",
            "Open dashboard: /management.html",
        ),
    ),
    ServiceModule(
        kind = ServiceKind.OPENCLAW,
        title = "OpenClaw",
        sessionName = "openclaw-gateway",
        startCommand = "tmux has-session -t openclaw-gateway 2>/dev/null || tmux new-session -d -s openclaw-gateway 'openclaw gateway'",
        stopCommand = "tmux kill-session -t openclaw-gateway 2>/dev/null || true",
        status = "Theo dõi qua tmux session",
        notes = listOf(
            "Session tmux: openclaw-gateway",
            "Start command: openclaw gateway",
            "Open action sẽ mở Termux để bạn thao tác tiếp",
        ),
    ),
)
