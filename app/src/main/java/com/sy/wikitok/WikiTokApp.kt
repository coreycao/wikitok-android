package com.sy.wikitok

import android.app.Application
import com.sy.wikitok.utils.Logger

/**
 * @author Yeung
 * @date 2025/3/18
 */
class WikiTokApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Logger.d(tag = "WikiTokApp", message = "onCreate")
    }
}