package com.sy.wikitok

import android.app.Application
import androidx.startup.AppInitializer

/**
 * @author Yeung
 * @date 2025/3/18
 */
class WikiTokApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initThreeTen()
    }

    private fun initThreeTen() {
        AppInitializer.getInstance(this@WikiTokApp)
            .initializeComponent(ThreeTenInitializer::class.java)
    }
}