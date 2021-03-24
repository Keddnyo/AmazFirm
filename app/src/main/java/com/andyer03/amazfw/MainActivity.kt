package com.andyer03.amazfw

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private val url = "https://schakal.ru/fw/firmwares_list.htm"
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permissionCheck = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        val webView = findViewById<WebView>(R.id.webView)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.forceDark = WebSettings.FORCE_DARK_AUTO
        }
        webView.loadUrl(url)
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(webView: WebView, errorCode: Int, description: String, failingUrl: String) {
                webView.loadUrl("about:blank")
                val alertDialog = AlertDialog.Builder(this@MainActivity)
                alertDialog.setTitle(getString(R.string.error))
                alertDialog.setMessage(getString(R.string.retry_connect))
                alertDialog.setNegativeButton(getString(R.string.refresh)) { dialog, _ ->
                    dialog.dismiss()
                    webView.reload()
                    webView.loadUrl(url)
                }
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
        webView.setDownloadListener { url, _, contentDisposition, mimeType, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription(getString(R.string.downloading))
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, getString(R.string.downloading), Toast.LENGTH_LONG).show()
        }
    }
    override fun onBackPressed() {
        val aboutDialog = AlertDialog.Builder(this)
        aboutDialog.setTitle(getString(R.string.app_name)+" "+BuildConfig.VERSION_NAME)
        aboutDialog.setMessage(getString(R.string.logic_credits)+"\n"+getString(R.string.app_credits))
        aboutDialog.setPositiveButton(getString(R.string.exit_title)) { _, _ ->
            finish()
        }
        aboutDialog.setNegativeButton(getString(R.string.back)) { dialog, _ ->
            dialog.dismiss()
        }
        aboutDialog.setNeutralButton(getString(R.string.refresh)) { dialog, _ ->
            dialog.dismiss()
            findViewById<WebView>(R.id.webView).loadUrl(url)
        }
        aboutDialog.show()
    }
}