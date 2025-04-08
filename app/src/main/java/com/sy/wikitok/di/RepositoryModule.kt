package com.sy.wikitok.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.dataStore
import com.sy.wikitok.network.AppUpdateApiService
import com.sy.wikitok.network.WikiApiService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 */

val repositoryModule = module {
    single { provideUserRepository(get(), androidApplication().dataStore) }
    single { provideWikiRepository(get(), get(), get(), get()) }
}

fun provideWikiRepository(
    wikiApiService: WikiApiService,
    userRepository: UserRepository,
    feedDao: FeedDao,
    favoriteDao: FavoriteDao
): WikiRepository {
    return WikiRepository(
        wikiApiService = wikiApiService,
        userRepository = userRepository,
        feedDao = feedDao,
        favDao = favoriteDao
    )
}

fun provideUserRepository(
    appUpdateApiService: AppUpdateApiService,
    dataStore: DataStore<Preferences>
): UserRepository {
    return UserRepository(
        appUpdateApiService = appUpdateApiService,
        dataStore = dataStore
    )
}