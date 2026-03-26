package com.adbtool

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    fun isReady(): Boolean {
        return Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    fun uninstall(context: Context, pkg: String): String {
        if (!isReady()) return "Shizuku belum aktif"

        return try {
            val process = Shizuku.newProcess(
                arrayOf("sh", "-c", "pm uninstall --user 0 $pkg"),
                null,
                null
            )

            val result = BufferedReader(InputStreamReader(process.inputStream)).readText()
            result.ifEmpty { "Success" }

        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}