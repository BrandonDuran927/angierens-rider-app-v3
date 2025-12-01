package com.brandon.angierens_rider.core.presentation

import android.app.Application
import android.util.Log
import com.brandon.angierens_rider.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import java.io.File


@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configure OSMDroid BEFORE any map views are created
        val cfg = Configuration.getInstance()

        // CRITICAL: Set user agent - required by OSM tile servers
        cfg.userAgentValue = "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME}"

        // Use INTERNAL storage (no permissions needed)
        cfg.osmdroidBasePath = filesDir
        cfg.osmdroidTileCache = File(filesDir, "osmdroid/tiles").apply {
            if (!exists()) mkdirs()
        }

        // Load from preferences
        cfg.load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        // Performance settings
        cfg.tileDownloadThreads = 8.toShort()
        cfg.tileFileSystemThreads = 8.toShort()
        cfg.tileDownloadMaxQueueSize = 40.toShort()
        cfg.tileFileSystemMaxQueueSize = 40.toShort()

        // Cache settings (30 days)
        cfg.expirationOverrideDuration = 1000L * 60 * 60 * 24 * 30
        cfg.expirationExtendedDuration = 1000L * 60 * 60 * 24 * 7

//        // Debug logging (disable in production)
//        cfg.isDebugMode = BuildConfig.DEBUG
//        cfg.isDebugTileProviders = BuildConfig.DEBUG
//        cfg.isDebugMapView = BuildConfig.DEBUG
//        cfg.isDebugMapTileDownloader = BuildConfig.DEBUG

        Log.d("MyApp", "OSMDroid configured - Cache: ${cfg.osmdroidTileCache}")
    }
}
