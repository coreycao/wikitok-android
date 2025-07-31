package com.sy.wikitok.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Database(
    entities = [FavoriteEntity::class, WikiEntity::class, MessageEntity::class, LanguageEntity::class],
    version = 2
)
@TypeConverters(MessageTypeConverter::class)
abstract class WikiDataBase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    abstract fun feedDao(): FeedDao

    abstract fun messageDao(): MessageDao

    abstract fun langDao(): LangDao
}