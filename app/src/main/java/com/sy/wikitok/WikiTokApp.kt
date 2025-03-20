package com.sy.wikitok

import android.app.Application
import com.sy.wikitok.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * @author Yeung
 * @date 2025/3/18
 */
class WikiTokApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WikiTokApp)
            modules(appModule)
        }
    }
}