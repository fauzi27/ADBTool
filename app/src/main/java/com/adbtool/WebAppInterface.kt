package com.adbtool

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import dev.rikka.shizuku.Shizuku
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.thread

class WebAppInterface(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun getApps() {
        thread {
            try {
                val pm = context.packageManager
                val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .sortedBy { pm.getApplicationLabel(it).toString().lowercase(Locale.ROOT) }
                val arr = JSONArray()
                for (app in apps) {
                    if (app.packageName == context.packageName) continue
                    val obj = JSONObject()
                    obj.put("name", pm.getApplicationLabel(app).toString())
                    obj.put("package", app.packageName)
                    val isSystem = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    obj.put("system", isSystem)
                    arr.put(obj)
                }
                sendToJs("onAppsLoaded", arr.toString())
            } catch (e: Throwable) {
                sendToJs("onError", "Failed to load app list: ${e.localizedMessage}")
            }
        }
    }

    @JavascriptInterface
    fun uninstall(packageName: String) {
        thread {
            if (!Shizuku.pingBinder()) {
                sendToJs("onError", "Shizuku service is not running.")
                return@thread
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                sendToJs("onError", "Shizuku permission not granted.\nPlease open Shizuku app to grant permission.")
                return@thread
            }
            try {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", "pm uninstall --user 0 $packageName"), null, null)
                val output = process.inputStream.bufferedReader().readText().trim()
                val error = process.errorStream.bufferedReader().readText().trim()
                val exitCode = process.waitFor()
                if (exitCode == 0 && (output == "Success" || output.isEmpty())) {
                    sendToJs("onUninstallResult", JSONObject()
                        .put("package", packageName)
                        .put("success", true)
                        .put("message", "Uninstalled successfully")
                        .toString())
                } else {
                    sendToJs("onUninstallResult", JSONObject()
                        .put("package", packageName)
                        .put("success", false)
                        .put("message", error.ifEmpty { output.ifEmpty { "Unknown error" } })
                        .toString())
                }
            } catch (e: Throwable) {
                sendToJs("onUninstallResult", JSONObject()
                    .put("package", packageName)
                    .put("success", false)
                    .put("message", "Exception: ${e.localizedMessage}")
                    .toString())
            }
        }
    }

    @JavascriptInterface
    fun checkShizuku() {
        thread {
            val result = JSONObject()
            result.put("active", Shizuku.pingBinder())
            result.put("permission", Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
            sendToJs("onShizukuStatus", result.toString())
        }
    }

    private fun sendToJs(fnName: String, payload: String) {
        mainHandler.post {
            (context as? MainActivity)?.let { act ->
                val js = "javascript:window.$fnName(${JSONObject.quote(payload)});"
                val wv = (act.findViewById(android.R.id.content) as? android.view.ViewGroup)?.getChildAt(0)
                if (wv is android.webkit.WebView) {
                    wv.evaluateJavascript(js, null)
                }
            }
        }
    }
}