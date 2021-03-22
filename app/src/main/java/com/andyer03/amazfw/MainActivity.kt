package com.andyer03.amazfw

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Apply saved theme on opening
        themeSwitchOnly()

        //Shared preferences Start
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        //Shared preferences End

        if (sharedPreference.getInt("Rotation", 1) == 0) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        } else if (sharedPreference.getInt("Rotation", 1) == 1) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }

        val webView = findViewById<WebView>(R.id.webView)
        Handler().postDelayed({
            webView.visibility = View.VISIBLE
        }, 2000)

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

        val permissionCheck = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            //requesting permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()

        val webView = findViewById<WebView>(R.id.webView)
        webView.isLongClickable = false
        webView.isHapticFeedbackEnabled = false
        webView.isLongClickable = true
        webView.setOnLongClickListener(OnLongClickListener { true })
        webView.isFocusableInTouchMode = false
        webView.isFocusable = false

        webView.loadUrl("https://schakal.ru/fw/firmwares_list.htm")

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
                        webView.loadUrl("https://schakal.ru/fw/firmwares_list.htm")
                    }
                    alertDialog.show()
                    webView.reload()
                    webView.loadUrl("https://schakal.ru/fw/firmwares_list.htm")
                    super.onReceivedError(webView, errorCode, description, failingUrl)
                }
            }

        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
    }

    class MyClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, Url: String): Boolean {
            view.loadUrl(Url)
            return true

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun themeSwitch() {
        val webView = findViewById<WebView>(R.id.webView)
        val webSettings = webView.settings

        //Shared preferences Start
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        //Shared preferences End

        if (sharedPreference.getInt("Theme", 0) == 1) {
            webSettings.forceDark = WebSettings.FORCE_DARK_OFF
            editor.putInt("Theme", 0)
            editor.apply()
        } else {
            webSettings.forceDark = WebSettings.FORCE_DARK_ON
            editor.putInt("Theme", 1)
            editor.apply()
        }
    }

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(Build.VERSION_CODES.Q)
    fun themeSwitchOnly() {
        val webView = findViewById<WebView>(R.id.webView)
        val webSettings = webView.settings

        //Shared preferences Start
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        //Shared preferences End

        if (sharedPreference.getInt("Theme", 1) == 1) {
            webSettings.forceDark = WebSettings.FORCE_DARK_OFF
        } else {
            webSettings.forceDark = WebSettings.FORCE_DARK_ON
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.about_button -> {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.about))
            builder.setMessage(getString(R.string.app_name)+
                    " "+
                    BuildConfig.VERSION_NAME+
                    "\n\n"+
                    getString(R.string.logics_credits)+
                    "\n"+
                    getString(R.string.app_credits))
            builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
            true
        }
        R.id.refresh_button -> {
            val webView = findViewById<WebView>(R.id.webView)
            webView.loadUrl("https://schakal.ru/fw/firmwares_list.htm")
            true
        }
        R.id.theme_button -> {
            themeSwitch()
            true
        }
        R.id.rotation_button -> {

            //Shared preferences Start
            val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
            var editor = sharedPreference.edit()
            //Shared preferences End

            if (sharedPreference.getInt("Rotation", 1) == 1) {

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
                Toast.makeText(applicationContext, getString(R.string.no_rotation), Toast.LENGTH_SHORT).show()

                editor.putInt("Rotation", 0)
                editor.apply()

            } else if (sharedPreference.getInt("Rotation", 1) == 0) {

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                Toast.makeText(applicationContext, getString(R.string.yes_rotation), Toast.LENGTH_SHORT).show()

                editor.putInt("Rotation", 1)
                editor.apply()

            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
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
    }

}