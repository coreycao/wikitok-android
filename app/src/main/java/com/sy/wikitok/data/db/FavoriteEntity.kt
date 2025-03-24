package com.sy.wikitok.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Entity(tableName = "tb_favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(index = true)
    val content: String,
    val imgUrl: String,
    val linkUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)