package com.adbtool

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject

class WebAppInterface(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    @JavascriptInterface
    fun getApps(): String {
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val arr = JSONArray()

        for (app in apps) {
            val obj = JSONObject()

            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            obj.put("name", pm.getApplicationLabel(app).toString())
            obj.put("package", app.packageName)
            obj.put("system", isSystem)

            arr.put(obj)
        }

        return arr.toString()
    }

    @JavascriptInterface
    fun uninstall(pkg: String): String {
        return ShizukuManager.uninstall(context, pkg)
    }

    @JavascriptInterface
    fun checkShizuku(): Boolean {
        return ShizukuManager.isReady()
    }
}