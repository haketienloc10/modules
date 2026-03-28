package com.example.modules

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CliProxyApiClient {
    suspend fun fetchSnapshot(settings: CliProxySettings): CliProxySnapshot = withContext(Dispatchers.IO) {
        val baseUrl = settings.baseUrl.trim().trimEnd('/')
        if (baseUrl.isBlank()) {
            return@withContext CliProxySnapshot(
                ok = false,
                status = "Thiếu base URL",
                details = "Chưa cấu hình địa chỉ CLIProxyAPI",
                endpoint = "",
                logs = emptyList(),
                configSummary = emptyList(),
            )
        }

        val configResult = getJsonObject("$baseUrl/v0/management/config", settings.managementKey)
        if (!configResult.ok || configResult.objectBody == null) {
            return@withContext CliProxySnapshot(
                ok = false,
                status = "Không truy cập được API",
                details = configResult.message,
                endpoint = baseUrl,
                logs = emptyList(),
                configSummary = emptyList(),
            )
        }

        val logsResult = getJsonObject("$baseUrl/v0/management/logs?limit=20", settings.managementKey)
        val logs = logsResult.objectBody?.optJSONArray("lines").toStringList()
        val config = configResult.objectBody
        val routing = config.optJSONObject("routing")
        val summary = buildList {
            add("logging-to-file: ${config.optBoolean("logging-to-file", false)}")
            add("usage-statistics-enabled: ${config.optBoolean("usage-statistics-enabled", false)}")
            add("debug: ${config.optBoolean("debug", false)}")
            add("routing.strategy: ${routing?.optString("strategy").orEmpty().ifBlank { "round-robin" }}")
        }

        CliProxySnapshot(
            ok = true,
            status = "Đang chạy",
            details = configResult.message,
            endpoint = baseUrl,
            logs = logs,
            configSummary = summary,
        )
    }

    private suspend fun getJsonObject(url: String, bearerToken: String): JsonResult = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 3000
                readTimeout = 3000
                if (bearerToken.isNotBlank()) {
                    setRequestProperty("Authorization", "Bearer $bearerToken")
                }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            }.orEmpty()
            connection.disconnect()
            if (code !in 200..299) {
                return@runCatching JsonResult(false, "HTTP $code ${body.ifBlank { "(không có nội dung)" }}", null)
            }
            JsonResult(true, "HTTP $code", JSONObject(body))
        }.getOrElse { error ->
            JsonResult(false, error.message ?: "Không rõ lỗi", null)
        }
    }
}

data class JsonResult(
    val ok: Boolean,
    val message: String,
    val objectBody: JSONObject?,
)

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            add(optString(index))
        }
    }
}
