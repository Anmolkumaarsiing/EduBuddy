package com.anmol.e_learning

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class DriveWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive_web_view)

        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()

        // Enable JavaScript (Some Drive links require JS to function properly)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Get Drive URL from intent
        val driveUrl = intent.getStringExtra("DRIVE_URL")
        if (driveUrl != null) {
            webView.loadUrl(driveUrl)
        }
    }
}
