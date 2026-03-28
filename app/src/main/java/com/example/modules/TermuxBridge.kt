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
    private const val TERMUX_PACKAGE = "com.termux"
    private const val TERMUX_BIN = "/data/data/com.termux/files/usr/bin/bash"
    private const val TERMUX_HOME = "/data/data/com.termux/files/home"

    fun start(context: Context, module: ServiceModule) {
        runShellCommand(context, module.startCommand, "Đã gửi lệnh start sang Termux")
    }

    fun stop(context: Context, module: ServiceModule) {
        runShellCommand(context, module.stopCommand, "Đã gửi lệnh stop sang Termux")
    }

    fun restart(context: Context, module: ServiceModule) {
        val command = "${module.stopCommand} >/dev/null 2>&1 || true; ${module.startCommand}"
        runShellCommand(context, command, "Đã gửi lệnh restart sang Termux")
    }

    fun openTermuxApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            toast(context, "Không tìm thấy app Termux")
        }
    }

    private fun runShellCommand(context: Context, command: String, toastMessage: String) {
        val intent = Intent(ACTION_RUN_COMMAND).apply {
            setPackage(TERMUX_PACKAGE)
            putExtra(EXTRA_COMMAND_PATH, TERMUX_BIN)
            putExtra(EXTRA_ARGUMENTS, arrayOf("-lc", command))
            putExtra(EXTRA_BACKGROUND, true)
            putExtra(EXTRA_WORKDIR, TERMUX_HOME)
        }
        try {
            context.sendBroadcast(intent)
            toast(context, toastMessage)
        } catch (_: SecurityException) {
            toast(context, "Thiếu quyền RUN_COMMAND của Termux")
        } catch (_: ActivityNotFoundException) {
            toast(context, "Không tìm thấy Termux")
        }
    }

    private fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
