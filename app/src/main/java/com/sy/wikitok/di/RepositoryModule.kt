package com.sy.wikitok.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.dataStore
import com.sy.wikitok.network.ApiService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 */

val repositoryModule = module {
    single { provideUserRepository(androidApplication().dataStore) }
    single { provideWikiRepository(get(), get(), get(), get()) }
}

fun provideWikiRepository(
    apiService: ApiService,
    userRepository: UserRepository,
    feedDao: FeedDao,
    favoriteDao: FavoriteDao
): WikiRepository {
    return WikiRepository(
        apiService = apiService,
        userRepository = userRepository,
        feedDao = feedDao,
        favDao = favoriteDao
    )
}

fun provideUserRepository(dataStore: DataStore<Preferences>): UserRepository {
    return UserRepository(dataStore)
}