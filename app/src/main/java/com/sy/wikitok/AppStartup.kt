package com.sy.wikitok

/**
 * @author Yeung
 * @date 2025/4/11
 */
import android.content.Context
import androidx.startup.Initializer
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.sy.wikitok.di.appModule
import com.sy.wikitok.utils.Logger as AppLogger

class KoinInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        startKoin {
            if (BuildConfig.DEBUG) androidLogger()
            androidContext(context)
            modules(appModule)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

class CoilInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        SingletonImageLoader.setSafe {
            val builder = ImageLoader.Builder(it)
                .crossfade(true)

            if (BuildConfig.DEBUG) {
                builder.logger(DebugLogger())
            }
            builder.build()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

class AppLoggerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (BuildConfig.DEBUG.not()) {
            AppLogger.setLogLevel(AppLogger.LogLevel.WARN)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}