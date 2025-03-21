package com.sy.wikitok.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Database(entities = [FavoriteEntity::class, WikiEntity::class], version = 1, exportSchema = false)
abstract class WikiDataBase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    abstract fun feedDao(): FeedDao
}