package com.sy.wikitok

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.sy.wikitok.utils.Logger as AppLogger
import com.sy.wikitok.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * @author Yeung
 * @date 2025/3/18
 */
class WikiTokApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppLogger()
        initKoin()
        initCoil()
    }

    private fun initAppLogger() {
        if (BuildConfig.DEBUG.not())
            AppLogger.setLogLevel(AppLogger.LogLevel.WARN)
    }

    private fun initKoin() {
        startKoin {
            if (BuildConfig.DEBUG) androidLogger()
            androidContext(this@WikiTokApp)
            modules(appModule)
        }
    }

    private fun initCoil() {
        SingletonImageLoader.setSafe { context->
            val builder = ImageLoader.Builder(context)
                .crossfade(true)

            if (BuildConfig.DEBUG) {
                builder.logger(DebugLogger())
            }
            builder.build()
        }
    }
}