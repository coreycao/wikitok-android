package com.sy.wikitok.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Dao
interface FeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFeeds(feeds: List<WikiEntity>): List<Long>

    @Query("SELECT * FROM tb_feeds ORDER BY timestamp DESC")
    suspend fun readFeeds(): List<WikiEntity>

    @Query("DELETE FROM tb_feeds")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAllFeeds(feeds: List<WikiEntity>) {
        deleteAll()
        saveFeeds(feeds)
    }
}