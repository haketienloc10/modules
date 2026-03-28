package com.example.modules

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast

object TermuxBridge {
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    private const val EXTRA_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    private const val EXTRA_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val EXTRA_COMMAND_LABEL = "com.termux.RUN_COMMAND_LABEL"
    private const val TERMUX_PACKAGE = "com.termux"
    private const val TERMUX_RUN_COMMAND_SERVICE = "com.termux.app.RunCommandService"
    private const val TERMUX_BIN = "/data/data/com.termux/files/usr/bin/bash"
    private const val TERMUX_HOME = "/data/data/com.termux/files/home"

    fun start(context: Context, module: ServiceModule): Boolean {
        return runShellCommand(context, module.startCommand, "start ${module.title}")
    }

    fun stop(context: Context, module: ServiceModule): Boolean {
        return runShellCommand(context, module.stopCommand, "stop ${module.title}")
    }

    fun restart(context: Context, module: ServiceModule): Boolean {
        val command = "${module.stopCommand}; ${module.startCommand}"
        return runShellCommand(context, command, "restart ${module.title}")
    }

    fun openTermuxApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            toast(context, "Không tìm thấy app Termux")
        }
    }

    private fun runShellCommand(context: Context, command: String, label: String): Boolean {
        val intent = Intent(ACTION_RUN_COMMAND).apply {
            setClassName(TERMUX_PACKAGE, TERMUX_RUN_COMMAND_SERVICE)
            putExtra(EXTRA_COMMAND_PATH, TERMUX_BIN)
            putExtra(EXTRA_ARGUMENTS, arrayOf("-lc", command))
            putExtra(EXTRA_BACKGROUND, true)
            putExtra(EXTRA_WORKDIR, TERMUX_HOME)
            putExtra(EXTRA_COMMAND_LABEL, label)
        }
        return try {
            context.startService(intent)
            toast(context, "Đã gửi lệnh $label sang Termux")
            true
        } catch (_: SecurityException) {
            toast(context, "Thiếu quyền RUN_COMMAND của Termux")
            false
        } catch (_: ActivityNotFoundException) {
            toast(context, "Không tìm thấy Termux")
            false
        } catch (_: IllegalStateException) {
            toast(context, "Termux từ chối chạy lệnh lúc này")
            false
        }
    }

    private fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
