package com.andyer03.amazfw

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private val url: String = "https://schakal.ru/fw/firmwares_list.htm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionCheck = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //requesting permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        val webView = findViewById<WebView>(R.id.webView)

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(webView: WebView, errorCode: Int, description: String, failingUrl: String) {
                try {
                    webView.stopLoading()
                } catch (e: Exception) {
                }
                if (webView.canGoBack()) {
                    webView.goBack()
                }
                webView.loadUrl("about:blank")
                val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                alertDialog.setTitle(getString(R.string.error))
                alertDialog.setMessage(getString(R.string.retry_connect))
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.retry)) { _, _ ->
                    alertDialog.dismiss()
                    webView.reload()
                    webView.goBack()
                }
                alertDialog.show()
                super.onReceivedError(webView, errorCode, description, failingUrl)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        val webView = findViewById<WebView>(R.id.webView)
        webView.loadUrl(url)

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimeType)
            request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Downloading file...")
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, ".zip")
            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(applicationContext, getString(R.string.downloading), Toast.LENGTH_LONG).show()

            webView.webViewClient = object : WebViewClient() {
                override fun onReceivedError(webView: WebView, errorCode: Int, description: String, failingUrl: String) {
                    try {
                        webView.stopLoading()
                    } catch (e: Exception) {
                    }
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                    webView.loadUrl("about:blank")
                    val alertDialog = AlertDialog.Builder(this@MainActivity).create()
                    alertDialog.setTitle(getString(R.string.error))
                    alertDialog.setMessage(getString(R.string.retry_connect))
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.retry)) { _, _ ->
                        alertDialog.dismiss()
                        webView.reload()
                        webView.goBack()
                    }
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.close)) { _, _ ->
                        alertDialog.dismiss()
                        webView.loadUrl(url)
                    }
                    alertDialog.show()
                    webView.reload()
                    webView.loadUrl(url)
                    super.onReceivedError(webView, errorCode, description, failingUrl)
                }
            }
        }
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.forceDark = WebSettings.FORCE_DARK_AUTO
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.about_button -> {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.about))
            builder.setMessage(getString(R.string.app_name)+
                    " "+
                    BuildConfig.VERSION_NAME+
                    "\n\n"+
                    getString(R.string.logic_credits)+
                    "\n"+
                    getString(R.string.app_credits))
            builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
            true
        }
        R.id.exit_button -> {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.exit_title))
            builder.setMessage(getString(R.string.exit_content))
            builder.setPositiveButton(android.R.string.yes) { _, _ ->
                super.onBackPressed()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    override fun onBackPressed() {
        findViewById<WebView>(R.id.webView).loadUrl(url)
    }
}