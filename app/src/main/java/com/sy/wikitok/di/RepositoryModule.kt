package com.sy.wikitok.di

import android.content.res.AssetManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.repository.AIChatRepository
import com.sy.wikitok.data.repository.ConfigRepository
import com.sy.wikitok.data.repository.GenAIRepository
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.data.repository.settingDataStore
import com.sy.wikitok.data.repository.summaryDataStore
import com.sy.wikitok.network.WikiApiService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 */

val repositoryModule = module {
    single { UserRepository(get(), androidApplication().settingDataStore) }
    single {
        provideWikiRepository(
            get(),
            get(),
            get(),
            androidApplication().settingDataStore,
            androidApplication().assets
        )
    }
    single { GenAIRepository(get(), androidApplication().summaryDataStore) }
    single { ConfigRepository(get()) }
    single { AIChatRepository(get()) }
}

fun provideWikiRepository(
    wikiApiService: WikiApiService,
    feedDao: FeedDao,
    favoriteDao: FavoriteDao,
    dataStore: DataStore<Preferences>,
    assets: AssetManager
): WikiRepository {
    return WikiRepository(
        wikiApiService = wikiApiService,
        feedDao = feedDao,
        favDao = favoriteDao,
        dataStore = dataStore,
        assets = assets
    )
}