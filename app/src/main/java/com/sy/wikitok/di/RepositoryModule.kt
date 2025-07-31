package com.sy.wikitok.di

import android.content.res.AssetManager
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.db.LangDao
import com.sy.wikitok.data.repository.AIChatRepository
import com.sy.wikitok.data.repository.ConfigRepository
import com.sy.wikitok.data.repository.GenAIRepository
import com.sy.wikitok.data.repository.UserRepository
import com.sy.wikitok.data.repository.WikiRepository
import com.sy.wikitok.data.repository.configDataStore
import com.sy.wikitok.data.repository.summaryDataStore
import com.sy.wikitok.network.WikiApiService
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/18
 */

val repositoryModule = module {
    single { UserRepository(get(), get()) }
    single {
        provideWikiRepository(
            get(),
            get(),
            get(),
            get(),
            androidApplication().assets
        )
    }
    single { GenAIRepository(get(), get(), androidApplication().summaryDataStore) }
    single { ConfigRepository(get(), androidApplication().configDataStore, get()) }
    single { AIChatRepository(get(), get()) }
}

fun provideWikiRepository(
    wikiApiService: WikiApiService,
    feedDao: FeedDao,
    favoriteDao: FavoriteDao,
    langDao: LangDao,
    assets: AssetManager
): WikiRepository {
    return WikiRepository(
        wikiApiService = wikiApiService,
        feedDao = feedDao,
        favDao = favoriteDao,
        langDao = langDao,
        assets = assets
    )
}