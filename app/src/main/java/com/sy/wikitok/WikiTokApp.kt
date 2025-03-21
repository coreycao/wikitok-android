package com.sy.wikitok

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.sy.wikitok.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * @author Yeung
 * @date 2025/3/18
 */
class WikiTokApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@WikiTokApp)
            modules(appModule)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .logger(DebugLogger())
            .crossfade(true)
            .build()
    }
}