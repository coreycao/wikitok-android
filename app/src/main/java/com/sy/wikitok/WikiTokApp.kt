package com.sy.wikitok

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.sy.wikitok.utils.Logger as AppLogger
import com.sy.wikitok.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.threeten.bp.zone.ZoneRulesProvider

/**
 * @author Yeung
 * @date 2025/3/18
 */

private const val SETTINGS_NAME = "settings"
val Application.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_NAME)

class WikiTokApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAppLogger()
        initKoin()
        initCoil()
        initThreeTen()
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

    private fun initThreeTen() {
        ZoneRulesProvider.getAvailableZoneIds()
    }
}