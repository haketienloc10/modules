package com.example.modules

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("modules_settings", Context.MODE_PRIVATE)

    fun load(): CliProxySettings {
        return CliProxySettings(
            baseUrl = prefs.getString(KEY_BASE_URL, "http://127.0.0.1:8317").orEmpty(),
            managementKey = prefs.getString(KEY_MANAGEMENT_KEY, "").orEmpty(),
        )
    }

    fun save(settings: CliProxySettings) {
        prefs.edit()
            .putString(KEY_BASE_URL, settings.baseUrl)
            .putString(KEY_MANAGEMENT_KEY, settings.managementKey)
            .apply()
    }

    private companion object {
        const val KEY_BASE_URL = "base_url"
        const val KEY_MANAGEMENT_KEY = "management_key"
    }
}
