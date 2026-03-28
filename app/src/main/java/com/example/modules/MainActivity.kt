package com.example.modules

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.modules.ui.theme.ModulesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModulesTheme {
                val context = LocalContext.current
                val viewModel: ModulesViewModel = viewModel(
                    factory = ModulesViewModelFactory(
                        settingsStore = SettingsStore(context),
                        cliProxyApiClient = CliProxyApiClient(),
                    ),
                )
                val state = viewModel.state

                ModulesScreen(
                    state = state,
                    onBaseUrlChange = viewModel::updateBaseUrl,
                    onManagementKeyChange = viewModel::updateManagementKey,
                    onRefreshAll = viewModel::refreshAll,
                    onRefreshModule = viewModel::refreshModule,
                    onStartModule = { module -> TermuxBridge.start(context, module) },
                    onStopModule = { module -> TermuxBridge.stop(context, module) },
                    onRestartModule = { module -> TermuxBridge.restart(context, module) },
                    onOpenModule = { module -> openModule(context, module) },
                )
            }
        }
    }
}
