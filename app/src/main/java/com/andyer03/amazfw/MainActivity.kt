package com.andyer03.amazfw

import android.Manifest
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val permissionCheck = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
        //    //requesting permission
        //    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        //}
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()

        val webView = findViewById<WebView>(R.id.webView)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        //Shared preferences Start
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        //Shared preferences End

        //webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
        //    val request = DownloadManager.Request(
        //            Uri.parse(url))
        //    request.setMimeType(mimeType)
        //    val cookies: String = CookieManager.getInstance().getCookie(url)
        //    request.addRequestHeader("cookie", cookies)
        //    request.addRequestHeader("User-Agent", userAgent)
        //    request.setDescription("Downloading File...")
        //    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
        //    request.allowScanningByMediaScanner()
        //    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        //    request.setDestinationInExternalPublicDir(
        //            Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
        //            url, contentDisposition, mimeType))
        //    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        //    dm.enqueue(request)
        //    Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
        //}

        webView.loadUrl("https://schakal.ru/fw/firmwares_list.htm")
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        floatingActionButton.setOnClickListener {
            if (sharedPreference.getInt("Theme", 0) == 1) {
                webSettings.forceDark = WebSettings.FORCE_DARK_OFF
                floatingActionButton.setImageResource(R.drawable.ic_moon)
                editor.putInt("Theme", 0)
                editor.apply()
            } else {
                webSettings.forceDark = WebSettings.FORCE_DARK_ON
                floatingActionButton.setImageResource(R.drawable.ic_sun)
                editor.putInt("Theme", 1)
                editor.apply()
            }
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
            builder.setMessage(getString(R.string.logics_credits)+"\n"+getString(R.string.app_credits))
            builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
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