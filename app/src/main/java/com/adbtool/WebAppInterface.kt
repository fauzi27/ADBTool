package com.adbtool

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun uninstall(pkg: String) {
        Toast.makeText(context, "Uninstall: $pkg", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getApps(): String {
        // sementara dummy
        return """
            ["com.facebook.katana","com.instagram.android","com.tiktok.app"]
        """
    }
}