package com.sy.wikitok.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Yeung
 * @date 2025/3/21
 */
@Entity(tableName = "tb_feeds")
class WikiEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val imgUrl: String,
    val linkUrl: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)