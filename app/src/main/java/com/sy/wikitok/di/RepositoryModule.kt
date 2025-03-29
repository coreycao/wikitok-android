package com.sy.wikitok.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.network.ApiService
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 *
 * repository module:
 * provide remote repository
 */

val repositoryModule = module {
    single { provideWikiRepository(get(), get(), get(), get()) }
}

fun provideWikiRepository(
    apiService: ApiService,
    dataStore: DataStore<Preferences>,
    feedDao: FeedDao,
    favoriteDao: FavoriteDao
): WikiRepository {
    return WikiRepository(
        dataStore = dataStore,
        apiService = apiService,
        feedDao = feedDao,
        favDao = favoriteDao
    )
}