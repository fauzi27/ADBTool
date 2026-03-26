package com.adbtool

import android.content.Context
import android.widget.Toast
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    fun isReady(): Boolean {
        return Shizuku.pingBinder() &&
               Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun uninstall(context: Context, pkg: String) {
        if (!isReady()) {
            Toast.makeText(context, "Shizuku belum siap", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "pm uninstall --user 0 $pkg"))

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readText()

            Toast.makeText(context, result, Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}