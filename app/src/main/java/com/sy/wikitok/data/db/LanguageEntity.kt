package com.sy.wikitok.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author Yeung
 * @date 2025/7/31
 */
@Entity(tableName = "tb_languages")
data class LanguageEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val flag: String,
    val api: String,
    val selected: Int
)