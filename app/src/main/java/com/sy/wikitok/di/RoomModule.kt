package com.sy.wikitok.di

import android.content.Context
import androidx.room.Room
import com.sy.wikitok.data.db.FavoriteDao
import com.sy.wikitok.data.db.FeedDao
import com.sy.wikitok.data.db.WikiDataBase
import org.koin.dsl.module

/**
 * @author Yeung
 * @date 2025/3/21
 */
val roomModule = module {
    single { provideFeedDao(get()) }
    single { provideFavoriteDao(get()) }
    single { provideDatabase(get()) }
}

private fun provideFeedDao(database: WikiDataBase): FeedDao {
    return database.feedDao()
}

private fun provideFavoriteDao(database: WikiDataBase): FavoriteDao {
    return database.favoriteDao()
}

private fun provideDatabase(context: Context): WikiDataBase {
    return Room.databaseBuilder(
        context,
        WikiDataBase::class.java,
        DB_NAME
    ).build()
}

private const val DB_NAME = "wikitok-db"